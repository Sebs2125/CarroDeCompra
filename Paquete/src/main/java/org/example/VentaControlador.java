package org.example;

import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;



// En esta clase con el nombre de la funcion ya se sabe su objectivo .
public class VentaControlador {
    private VentaGestion ventaGestion = VentaGestion.getInstance();

    public void mostrarVentas(Context ctx) {
        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");

        if (usuarioActual == null) {
            ctx.redirect("/login");
            return;
        }

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("ventas", ventaGestion.obtenerTodasLasVentas());
        modelo.put("usuario", usuarioActual);

        CarritoDeCompra carro = ctx.sessionAttribute("carro");
        int cantidadCarro = (carro != null) ? carro.getCantidadProductos() : 0;
        modelo.put("cantidadCarro", cantidadCarro);

        ctx.render("/templates/ventas.html", modelo);
    }
}