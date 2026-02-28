package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


//Esta clase con lo nombre la funciones ya se sabe cual es su funcion .
public class VentaGestion {
    private static VentaGestion instance;
    private List<Venta> ventas;
    private AtomicInteger contadorId;

    private VentaGestion() {
        ventas = new ArrayList<>();
        contadorId = new AtomicInteger(1);
    }

    public static VentaGestion getInstance() {
        if (instance == null) {
            instance = new VentaGestion();
        }
        return instance;
    }

    public void registrarVenta(String cliente, List<Producto> productos)
    {
        int id = contadorId.getAndIncrement();
        Venta venta = new Venta(id, cliente, productos);
        ventas.add(venta);
        System.out.println("Venta registrada - ID: " + id + ", Cliente: " + cliente + ", Total: RD$" + venta.getTotal());
    }

    public List<Venta> obtenerTodasLasVentas() {
        return new ArrayList<>(ventas);
    }

    public Venta obtenerVentaPorId(int id) {
        return ventas.stream()
                .filter(v -> v.getId() == id)
                .findFirst()
                .orElse(null);
    }
}