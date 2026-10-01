/// ==========================================
/// MODELOS DE DATOS Y MOTOR DE REPROGRAMACIÓN
/// DocentePro App - Entregable Flutter/Dart
/// ==========================================

import 'dart:core';

/// Estado de la planificación de una clase diaria
enum EstadoClase {
  pendiente,
  dictada,
  noDictada,
}

/// Representa a un Docente (Usuario principal)
class Docente {
  final String id;
  final String nombre;
  final String institucion;
  final String especialidad;
  final String fotoAvatarUrl;

  Docente({
    required this.id,
    required this.nombre,
    required this.institucion,
    required this.especialidad,
    required this.fotoAvatarUrl,
  });

  Map<String, dynamic> toJson() => {
    'id': id,
    'nombre': nombre,
    'institucion': institucion,
    'especialidad': especialidad,
    'fotoAvatarUrl': fotoAvatarUrl,
  };

  factory Docente.fromJson(Map<String, dynamic> json) => Docente(
    id: json['id'],
    nombre: json['nombre'],
    institucion: json['institucion'],
    especialidad: json['especialidad'],
    fotoAvatarUrl: json['fotoAvatarUrl'],
  );
}

/// Representa una Materia o Curso dictado
class MateriaCurso {
  final String id;
  final String nombre;
  final String aula;
  
  /// Días de la semana en que se dicta (1 = Lunes, 7 = Domingo)
  final Set<int> diasDictadoSemanales; 

  MateriaCurso({
    required this.id,
    required this.nombre,
    required this.aula,
    required this.diasDictadoSemanales,
  });
}

/// Representa un Alumno del curso
class Alumno {
  final String id;
  final String nombre;
  
  /// Historial de asistencias: Mapa de fecha a asistió (true/false)
  final Map<DateTime, bool> historialAsistencia;
  
  /// Historial de notas: Mapa de descripción/tarea a calificación decimal
  final Map<String, double> historialNotas;

  Alumno({
    required this.id,
    required this.nombre,
    required this.historialAsistencia,
    required this.historialNotas,
  });

  /// Retorna porcentaje de inasistencias
  double get porcentajeFaltas {
    if (historialAsistencia.isEmpty) return 0.0;
    int faltas = historialAsistencia.values.where((asistio) => !asistio).length;
    return (faltas / historialAsistencia.length) * 100;
  }

  /// Retorna promedio escolar actual
  double get promedioNotas {
    if (historialNotas.isEmpty) return 0.0;
    double suma = historialNotas.values.reduce((value, element) => value + element);
    return suma / historialNotas.length;
  }

  /// Alerta temprana: si faltó más del 15% o promedio inferior a 6.0
  bool get tieneAlertaTemprana => porcentajeFaltas > 15.0 || promedioNotas < 6.0;
}

/// Planificación Diaria de cada clase de una materia
class PlanificacionDiaria {
  final String id;
  final int nroClase; // Ej: Clase 1, Clase 2...
  final String unidad;
  final String contenido;
  final String actividades;
  
  /// Fecha física asignada en el calendario
  DateTime? fechaAsignada;
  
  /// Estado actual del dictado
  EstadoClase estado;

  PlanificacionDiaria({
    required this.id,
    required this.nroClase,
    required this.unidad,
    required this.contenido,
    required this.actividades,
    this.fechaAsignada,
    this.estado = EstadoClase.pendiente,
  });

  PlanificacionDiaria copyWith({
    String? id,
    int? nroClase,
    String? unidad,
    String? contenido,
    String? actividades,
    DateTime? fechaAsignada,
    EstadoClase? estado,
  }) {
    return PlanificacionDiaria(
      id: id ?? this.id,
      nroClase: nroClase ?? this.nroClase,
      unidad: unidad ?? this.unidad,
      contenido: contenido ?? this.contenido,
      actividades: actividades ?? this.actividades,
      fechaAsignada: fechaAsignada ?? this.fechaAsignada,
      estado: estado ?? this.estado,
    );
  }
}

/// LÓGICA DE PROGRAMACIÓN Y REPROGRAMACIÓN EN CASCADA
/// Esta clase contiene la lógica que desplaza automáticamente las clases
/// cuando ocurren interrupciones (feriados, paros, licencias, etc.) o cuando la clase se cancela.
class MotorReplanificacion {
  
  /// Normaliza una fecha eliminando horas, minutos y segundos para comparación exacta
  static DateTime _normalizarFecha(DateTime fecha) {
    return DateTime(fecha.year, fecha.month, fecha.day);
  }

  /// Verifica si una fecha coincide con el listado de interrupciones (feriados, paros, licencias)
  static bool _esFechaInterrumpida(DateTime fecha, Set<DateTime> interrupciones) {
    final normalizada = _normalizarFecha(fecha);
    return interrupciones.any((i) => _normalizarFecha(i) == normalizada);
  }

  /// Reprograma una secuencia de clases diarias en cascada
  /// 
  /// [planificaciones] Lista de clases ordenadas cronológica/pedagógicamente (Clase 1, 2, 3...)
  /// [diasDictado] Días de la semana permitidos para la materia (1 = Lunes, 7 = Domingo)
  /// [fechaInicio] Fecha a partir de la cual se asigna la planificación
  /// [interrupciones] Conjunto de fechas inhábiles (Feriados, Paros, etc.)
  /// 
  /// Beneficios del algoritmo:
  /// - Preserva de forma absoluta el orden pedagógico secuencial.
  /// - Excluye automáticamente fines de semana (según [diasDictado]) e interrupciones.
  /// - Bloquea en el calendario las clases ya "Dictadas" (no las reescribe para mantener el historial real).
  /// - Si una clase se marca como "No Dictada" o coincide con un feriado, el contenido
  ///   asociado y todo el temario subsiguiente se arrastra automáticamente al siguiente día hábil disponible.
  static List<PlanificacionDiaria> reprogramarEnCascada({
    required List<PlanificacionDiaria> planificaciones,
    required Set<int> diasDictado,
    required DateTime fechaInicio,
    required Set<DateTime> interrupciones,
  }) {
    // Si la materia no cuenta con días de dictado asignados, es imposible agendar
    if (diasDictado.isEmpty) return planificaciones;

    List<PlanificacionDiaria> resultado = [];
    DateTime cursorFecha = _normalizarFecha(fechaInicio);

    // 1. Identificar fechas que ya fueron Dictadas o Consolidadas en el pasado.
    // Estas fechas quedan bloqueadas para que nuevas clases flotantes no se encima sobre ellas.
    final Set<DateTime> fechasBloqueadasHistorial = planificaciones
        .where((p) => p.estado == EstadoClase.dictada && p.fechaAsignada != null)
        .map((p) => _normalizarFecha(p.fechaAsignada!))
        .toSet();

    for (var plani in planificaciones) {
      // Regla de Oro: Si la clase ya fue dictada con éxito, se respeta intacta su fecha e historial
      if (plani.estado == EstadoClase.dictada) {
        resultado.add(plani);
        // Desplazar el cursor para evitar asignar futuras clases en esa misma fecha
        if (plani.fechaAsignada != null) {
          final fechaNorm = _normalizarFecha(plani.fechaAsignada!);
          if (fechaNorm.isAtSameMomentAs(cursorFecha) || fechaNorm.isAfter(cursorFecha)) {
            cursorFecha = fechaNorm.add(const Duration(days: 1));
          }
        }
        continue;
      }

      // Para clases Pendientes o de tipo No Dictadas, buscamos el siguiente día hábil de dictado disponible
      DateTime fechaAsignadaParaEstaClase = cursorFecha;
      
      while (true) {
        final diaSemana = fechaAsignadaParaEstaClase.weekday;
        final estaNormalizada = _normalizarFecha(fechaAsignadaParaEstaClase);

        // Validaciones obligatorias de fecha hábil:
        // A. Debe ser parte de los días de dictado semanales de la materia.
        // B. No debe ser Feriado, Paro ni Licencia.
        // C. No debe estar ya ocupada por una clase "Dictada" del historial.
        bool esDiaDictadoValido = diasDictado.contains(diaSemana);
        bool esInhabil = _esFechaInterrumpida(estaNormalizada, interrupciones);
        bool estaOcupadaPorHistorial = fechasBloqueadasHistorial.contains(estaNormalizada);

        if (esDiaDictadoValido && !esInhabil && !estaOcupadaPorHistorial) {
          // Si el estado de la clase es "No Dictada" (significa que tocaba hoy pero se suspendió o canceló),
          // ese día específico se considera inhábil/perdido para el dictado real. Por ende, la clase
          // se desplaza al *siguiente* día hábil, marcando esta fecha actual como pérdida pedagógica.
          if (plani.estado == EstadoClase.noDictada) {
            // El día se pierde, asignamos la clase a 'noDictada' en esta fecha histórica para registro,
            // pero su CONTENIDO se duplicará/arrastrará adelante. Para reflejar la reprogramación
            // pura en cascada sin pérdida de contenido, arrastramos el contenido al siguiente día hábil.
            // Para eso, mantenemos esta clase en estado pendiente y avanzamos un día para buscarle fecha.
            fechaAsignadaParaEstaClase = fechaAsignadaParaEstaClase.add(const Duration(days: 1));
            continue;
          }

          // Se halló un día válido. Se guarda para la clase actual.
          break;
        }

        // Si no cumple, avanzamos al día siguiente de calendario
        fechaAsignadaParaEstaClase = fechaAsignadaParaEstaClase.add(const Duration(days: 1));
      }

      // Guardamos la clase reprogramada
      resultado.add(plani.copyWith(
        fechaAsignada: fechaAsignadaParaEstaClase,
        // Al reprogramar en cascada, si estaba "noDictada" o "pendiente", hereda estado "pendiente" en la nueva fecha
        estado: plani.estado == EstadoClase.noDictada ? EstadoClase.noDictada : EstadoClase.pendiente,
      ));

      // El cursor para la siguiente clase se inicia al menos un día después
      cursorFecha = fechaAsignadaParaEstaClase.add(const Duration(days: 1));
    }

    return resultado;
  }
}
