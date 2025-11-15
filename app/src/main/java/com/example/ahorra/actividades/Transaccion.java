package com.example.ahorra.actividades;

import java.util.Date;

public class Transaccion {
    private String categoria;
    private String hora;
    private double monto;
    private String tipo; // "Ingreso" o "Gasto"
    private int icono; // ej. R.drawable.ic_comida
    private Date fecha;

    // Constructor
    public Transaccion(String categoria, String hora, double monto, String tipo, int icono, Date fecha) {
        this.categoria = categoria;
        this.hora = hora;
        this.monto = monto;
        this.tipo = tipo;
        this.icono = icono;
        this.fecha = fecha;
    }

    // Getters (métodos para obtener los datos)
    public String getCategoria() { return categoria; }
    public String getHora() { return hora; }
    public double getMonto() { return monto; }
    public String getTipo() { return tipo; }
    public int getIcono() { return icono; }
    public Date getFecha() { return fecha; }
}