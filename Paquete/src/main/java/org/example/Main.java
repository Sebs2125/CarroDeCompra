package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;

public class Main {

    public static void main(String[] args) {

        UsuarioGestion usuarioGestion = UsuarioGestion.getInstance();
        ProductoGestion productoGestion = ProductoGestion.getInstance();
        VentaGestion ventaGestion = VentaGestion.getInstance();

        AutenticarUsuarioControlador autenticarUsuarioControlador = new AutenticarUsuarioControlador();
        ProductoControlador productoControlador = new ProductoControlador();
        CarroControlador carroControlador = new CarroControlador();
        VentaControlador ventaControlador = new VentaControlador();

        Javalin app = Javalin.create(set -> {
            set.staticFiles.add("/public", Location.CLASSPATH);
            set.fileRenderer(new JavalinThymeleaf());
        }).start(8080);



        app.get("/", ctx -> ctx.redirect("/productos"));

        app.get("/login", autenticarUsuarioControlador::mostrarPaginaLogin);
        app.post("/login", autenticarUsuarioControlador::login);
        app.get("/logout", autenticarUsuarioControlador::logout);

        app.get("/productos", productoControlador::mostrarProducto);

        app.get("/carrito", carroControlador::mostrarCarro);
        app.post("/carrito/add", carroControlador::addAlCarro);
        app.post("/carrito/eliminar", carroControlador::eliminarDelCarro);
        app.post("/carrito/limpiar", carroControlador::limpiarCarro);

        app.post("/carrito/procesar", carroControlador::procesarCompra);

        app.get("/ventas", ventaControlador::mostrarVentas);

        app.get("/admin", productoControlador::mostrarAdminPanel);
        app.post("/admin/productos", productoControlador::crearProducto);
        app.post("/admin/productos/actualizar", productoControlador::actualizarProducto);
        app.post("/admin/productos/{id}/delete", productoControlador::deletearProducto);

        app.error(404, ctx -> {
            ctx.result("Página no encontrada - 404");
        });

        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500).result("Error del servidor - 500");
            e.printStackTrace();
        });

        System.out.println("Usuarios cargados: " + usuarioGestion.todosLosUsuarios().size());
        System.out.println("Productos cargados: " + productoGestion.getListaProductos().size());
        System.out.println("Servidor HTTP iniciado en: http://localhost:8080");
        System.out.println("========================================");

        System.out.println("\nUsuarios disponibles:");
        System.out.println("  Admin:    usuario=admin, password=admin");
        System.out.println("  Cliente1: usuario=usuario, password=usuario1");
        System.out.println("  Cliente2: usuario=usuario2, password=usuario2");
    }
}