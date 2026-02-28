package org.example;

import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


// Esta clase los nombre de la funciones dicen lo que hacen .
public class CarroControlador {
    private ProductoGestion productoGestion = ProductoGestion.getInstance();
    private VentaGestion ventaGestion = VentaGestion.getInstance();

        //agregar carrito
    public void addAlCarro(Context ctx) {
        try {
            int productoId = Integer.parseInt(ctx.formParam("productoId"));
            int cantidad = Integer.parseInt(ctx.formParam("cantidad"));

            Producto producto = productoGestion.obtenerProductoPorId(productoId).orElse(null);

            if (producto == null) {
                ctx.status(404).result("El producto no existe");
                return;
            }

            if (cantidad <= 0) {
                ctx.status(400).result("Cantidad invalida");
                return;
            }

            if (!productoGestion.poseeInventario(productoId, cantidad)) {
                ctx.status(400).result("Inventario insuficiente. Stock disponible: " + producto.getInventario());
                return;
            }

            CarritoDeCompra carro = ctx.sessionAttribute("carro");

            if (carro == null) {
                carro = new CarritoDeCompra();
                ctx.sessionAttribute("carro", carro);
            }

            for (int i = 0; i < cantidad; i++) {
                carro.agregarProducto(producto);
            }

            System.out.println("Producto agregado - ID: " + productoId + ", Cantidad: " + cantidad);
            ctx.redirect("/productos");

        } catch (Exception e) {
            System.out.println("Error al agregar al carrito: " + e.getMessage());
            ctx.status(400).result("Error al agregar al carrito: " + e.getMessage());
        }
    }

    public void mostrarCarro(Context ctx) {
        CarritoDeCompra carro = ctx.sessionAttribute("carro");

        if (carro == null) {
            carro = new CarritoDeCompra();
        }

        Map<Integer, ProductoCarritoDTO> productosAgrupados = new HashMap<>();

        for (Producto p : carro.getListaProductos()) {
            if (productosAgrupados.containsKey(p.getId())) {
                ProductoCarritoDTO dto = productosAgrupados.get(p.getId());
                dto.setCantidad(dto.getCantidad() + 1);
                dto.setTotal(dto.getCantidad() * p.getPrecio());
            } else {
                ProductoCarritoDTO dto = new ProductoCarritoDTO();
                dto.setId(p.getId());
                dto.setNombre(p.getNombre());
                dto.setPrecioUnitario(p.getPrecio());
                dto.setCantidad(1);
                dto.setTotal(p.getPrecio());
                productosAgrupados.put(p.getId(), dto);
            }
        }

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("productosAgrupados", productosAgrupados.values());
        modelo.put("total", carro.getTotal());
        modelo.put("cantidadCarro", carro.getCantidadProductos());
        modelo.put("carroVacio", carro.getCantidadProductos() == 0);

        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");
        modelo.put("usuario", usuarioActual);

        ctx.render("/templates/carrito.html", modelo);
    }

    public void eliminarDelCarro(Context ctx) {
        try {
            int productoId = Integer.parseInt(ctx.formParam("productoId"));

            CarritoDeCompra carro = ctx.sessionAttribute("carro");

            if (carro != null) {
                carro.getListaProductos().removeIf(p -> p.getId() == productoId);
                System.out.println("Producto eliminado del carrito - ID: " + productoId);
            }

            ctx.redirect("/carrito");

        } catch (Exception e) {
            System.out.println("Error al eliminar del carrito: " + e.getMessage());
            ctx.status(400).result("Error al eliminar producto");
        }
    }

    public void limpiarCarro(Context ctx) {
        CarritoDeCompra carro = ctx.sessionAttribute("carro");

        if (carro != null) {
            carro.limpiar();
            System.out.println("Carrito limpiado completamente");
        }

        ctx.redirect("/carrito");
    }

    public void procesarCompra(Context ctx) {
        CarritoDeCompra carro = ctx.sessionAttribute("carro");

        if (carro == null || carro.getCantidadProductos() == 0) {
            ctx.redirect("/carrito?error=carrito_vacio");
            return;
        }

        String nombreCliente = ctx.formParam("nombreCliente");

        if (nombreCliente == null || nombreCliente.trim().isEmpty()) {
            ctx.redirect("/carrito?error=cliente_requerido");
            return;
        }

        Map<Integer, Long> conteoProductos = carro.getListaProductos().stream()
                .collect(Collectors.groupingBy(Producto::getId, Collectors.counting()));

        for (Map.Entry<Integer, Long> entry : conteoProductos.entrySet()) {
            int productoId = entry.getKey();
            int cantidad = entry.getValue().intValue();

            if (!productoGestion.poseeInventario(productoId, cantidad)) {
                Producto p = productoGestion.obtenerProductoPorId(productoId).orElse(null);
                String nombre = p != null ? p.getNombre() : "Producto";
                ctx.redirect("/carrito?error=sin_stock&producto=" + nombre);
                return;
            }
        }

        for (Map.Entry<Integer, Long> entry : conteoProductos.entrySet()) {
            productoGestion.reducirInventario(entry.getKey(), entry.getValue().intValue());
        }

        List<Producto> productosVendidos = new ArrayList<>(carro.getListaProductos());
        ventaGestion.registrarVenta(nombreCliente, productosVendidos);

        carro.limpiar();

        System.out.println("Compra procesada exitosamente para: " + nombreCliente);
        ctx.redirect("/ventas?success=compra_realizada");
    }

    public static class ProductoCarritoDTO {
        private int id;
        private String nombre;
        private double precioUnitario;
        private int cantidad;
        private double total;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public double getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }

        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
    }
}