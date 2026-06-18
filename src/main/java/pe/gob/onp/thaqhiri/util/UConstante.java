package pe.gob.onp.thaqhiri.util;

public class UConstante {

	public static Integer RESULTADO_NO_ENCONTRO_FILAS = 0;
	public static Integer RESULTADO_SI_ENCONTRO_FILAS = 1;
	public static Integer RESULTADO_ERROR = -1;	
	public static Integer RESULTADO_EXITOSO = 2;
	public static Integer RESULTADO_NO_PASA_VALIDACION = 3;
	
	public static Integer ACTIVO = 1;
	public static Integer INACTIVO = 0;
	// [CHANGE][autor: cormenos@onp.gob.pe][fecha: 2026-01-16 11:47 UTC-5 (Lima)][desc: Soporta MaterialLocalizations para es_PE/es_ES (historial por fecha)][obj: UConstante ST_REGI]
	public static Integer ACTIVO_REGI = 1;
	public static Integer INACTIVO_REGI = 0;
	
	public static String ACTIVO_DESCRIPCION = "Activo";
	public static String INACTIVO_DESCRIPCION = "Inactivo";
	
	public static String ORDEN_ASCENDENTE = "asc";
	public static String ORDEN_DESCENDENTE = "des";
	
	// Encabezados de las columnas de la plantilla XLS de carga masiva del plan de visitas
	public static String XLS_PLAN_NOMBRE_COLUMNA_COLABORADOR = "Colaborador";
	public static String XLS_PLAN_NOMBRE_COLUMNA_FECHA_PLAN = "FechaPlan (dd/MM/yyyy)";
	public static String XLS_PLAN_NOMBRE_COLUMNA_ORDEN = "Orden";
	public static String XLS_PLAN_NOMBRE_COLUMNA_DESTINO = "Destino";
	public static String XLS_PLAN_NOMBRE_COLUMNA_DIRECCION = "Direccion";
	public static String XLS_PLAN_NOMBRE_COLUMNA_HORA_CITA = "HoraCita (HH:mm)";
	public static String XLS_PLAN_NOMBRE_COLUMNA_PRIORIDAD = "Prioridad (MUY_ALTA|ALTA|NORMAL)";
	public static String XLS_PLAN_NOMBRE_COLUMNA_PV = "PlantillaPV";
	
	
	public static Integer TIPO_TRABAJO_CAMPO = 1;
	public static Integer TIPO_TRABAJO_NO_CAMPO = 2;
	
}
