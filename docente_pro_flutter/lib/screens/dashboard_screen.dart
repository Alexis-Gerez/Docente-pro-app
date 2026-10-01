import 'package:flutter/material.dart';
import '../models/planificacion_model.dart';

/// ==========================================================
/// PANTALLA PRINCIPAL: DASHBOARD DE CONTROL DIARIO
/// DocentePro App - Entregable Flutter/Dart (Material Design 3)
/// ==========================================================

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({Key? key}) : super(key: key);

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  // Datos simulados para demostrar el funcionamiento en UI de forma realista
  late Docente _docente;
  late List<Alumno> _alumnosConAlerta;
  late Map<String, dynamic> _proximaClase;
  late PlanificacionDiaria _planificacionHoy;

  @override
  void initState() {
    super.initState();
    _cargarDatosSimulados();
  }

  void _cargarDatosSimulados() {
    // 1. Docente
    _docente = Docente(
      id: 'doc_1',
      nombre: 'Prof. Alejandro Gómez',
      institucion: 'E.T. N° 12 Libertador General San Martín',
      especialidad: 'Informática Aplicada & Robótica',
      fotoAvatarUrl: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150',
    );

    // 2. Alumnos con Alerta Temprana (> 15% asistencia o < 6.0 promedio)
    _alumnosConAlerta = [
      Alumno(
        id: 'al_1',
        nombre: 'Sofía Rodríguez',
        historialAsistencia: {
          DateTime(2026, 5, 20): true,
          DateTime(2026, 5, 22): false,
          DateTime(2026, 5, 25): false,
          DateTime(2026, 5, 27): true,
          DateTime(2026, 5, 29): false, // 3 faltas de 5 clases = 60% faltas
        },
        historialNotas: {'Parcial 1': 7.5, 'TP Integrador': 8.0},
      ),
      Alumno(
        id: 'al_2',
        nombre: 'Lucas Benítez',
        historialAsistencia: {
          DateTime(2026, 5, 20): true,
          DateTime(2026, 5, 22): true,
          DateTime(2026, 5, 25): true,
          DateTime(2026, 5, 27): true,
        },
        historialNotas: {'Parcial 1': 4.5, 'Lección Oral': 5.0}, // Promedio 4.75 (< 6.0)
      ),
      Alumno(
        id: 'al_3',
        nombre: 'Mateo Álvarez',
        historialAsistencia: {
          DateTime(2026, 5, 20): false,
          DateTime(2026, 5, 22): true,
          DateTime(2026, 5, 25): false,
        }, // 66.6% de faltas
        historialNotas: {'Parcial 1': 3.5}, // Nota baja
      ),
    ];

    // 3. Próxima Clase
    _proximaClase = {
      'materia': 'Programación Orientada a Objetos',
      'aula': 'Aula 402 - 4° Año',
      'horario': '08:00 - 09:30 hs (Hoy)',
    };

    // 4. Planificación del día
    _planificacionHoy = PlanificacionDiaria(
      id: 'pl_clase3',
      nroClase: 3,
      unidad: 'Unidad II: Programación Estructurada',
      contenido: 'Estructuras de Control Iterativas (Lazo For / While)',
      actividades: 'Resolución individual de guía práctica en PC sobre acumuladores.',
    );
  }

  @override
  Widget build(BuildContext context) {
    // Definimos un esquema de color pastel, relajante y profesional para mitigar fatiga visual
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Scaffold(
      backgroundColor: colorScheme.surfaceVariant.withOpacity(0.3),
      appBar: AppBar(
        title: const Text(
          'DocentePro',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        elevation: 0,
        backgroundColor: Colors.transparent,
        actions: [
          IconButton(
            icon: const Icon(Icons.notifications_none_outlined),
            onPressed: () {},
          ),
          IconButton(
            icon: const Icon(Icons.sync),
            tooltip: 'Sincronizar Offline',
            onPressed: () {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('Datos guardados localmente (Listo para Firebase).'),
                  backgroundColor: Colors.teal,
                ),
              );
            },
          ),
        ],
      ),
      
      // MENÚ LATERAL (Navigation Drawer) estilizado con Material Design 3
      drawer: Drawer(
        child: Column(
          children: [
            // Encabezado estilizado con Avatar circular, Información del docente e Institución
            UserAccountsDrawerHeader(
              decoration: BoxDecoration(
                // Pastel relajante (azul/celeste profesional profundo)
                color: colorScheme.primaryContainer,
              ),
              currentAccountPicture: Container(
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  border: Border.all(color: colorScheme.onPrimaryContainer, width: 2),
                ),
                child: const CircleAvatar(
                  radius: 36,
                  // Imagen placeholder o foto del docente
                  backgroundImage: NetworkImage('https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150'),
                  backgroundColor: Colors.transparent,
                ),
              ),
              accountName: Text(
                _docente.nombre,
                style: TextStyle(
                  color: colorScheme.onPrimaryContainer,
                  fontWeight: FontWeight.bold,
                  fontSize: 16,
                ),
              ),
              accountEmail: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    _docente.especialidad,
                    style: TextStyle(
                      color: colorScheme.onPrimaryContainer.withOpacity(0.8),
                      fontSize: 12,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    _docente.institucion,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      color: colorScheme.onPrimaryContainer.withOpacity(0.8),
                      fontSize: 11,
                      fontWeight: FontWeight.w300,
                    ),
                  ),
                ],
              ),
            ),
            
            // Ítems de navegación rápida enfocada
            ListTile(
              leading: Icon(Icons.dashboard, color: colorScheme.primary),
              title: const Text('Dashboard de Control'),
              selected: true,
              selectedTileColor: colorScheme.primary.withOpacity(0.08),
              onTap: () => Navigator.pop(context),
            ),
            ListTile(
              leading: const Icon(Icons.menu_book),
              title: const Text('Planificaciones Diarias'),
              onTap: () {},
            ),
            ListTile(
              leading: const Icon(Icons.people),
              title: const Text('Asistencia Gestual Agile'),
              onTap: () {},
            ),
            ListTile(
              leading: const Icon(Icons.calendar_month),
              title: const Text('Calendario Escolar & Feriados'),
              onTap: () {},
            ),
            const Divider(),
            ListTile(
              leading: const Icon(Icons.settings),
              title: const Text('Configuración Offline'),
              onTap: () {},
            ),
            const Spacer(),
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 16.0),
              child: Text(
                'v1.0.0 - Modo Offline Guardado Local',
                style: TextStyle(fontSize: 11, color: Colors.grey[500]),
              ),
            ),
          ],
        ),
      ),

      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Saludo de bienvenida personalizado
              Text(
                '¡Hola, ${_docente.nombre.split(' ').last}! 👋',
                style: theme.textTheme.headlineMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: colorScheme.onSurface,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                'Este es tu estado actual para el día de hoy.',
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: colorScheme.onSurface.withOpacity(0.6),
                ),
              ),
              const SizedBox(height: 20),

              // CARD 1: PRÓXIMA CLASE
              _buildProximaClaseCard(colorScheme, theme),
              const SizedBox(height: 16),

              // CARD 2: TEMA DEL DÍA (ELEMENTO DE PLANIFICACIÓN)
              _buildTemaDelDiaCard(colorScheme, theme),
              const SizedBox(height: 20),

              // PANEL 3: ALERTAS TEMPRANAS DE ALUMNOS (EN ROJO)
              _buildAlertasTempranasPanel(colorScheme, theme),
              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }

  /// Construye la tarjeta de Próxima Clase (Materia, Aula, Horario)
  Widget _buildProximaClaseCard(ColorScheme colorScheme, ThemeData theme) {
    return Card(
      elevation: 0,
      color: Colors.blue[50] ?? const Color(0xFFE3F2FD), // Color pastel azul refrescante
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(color: Colors.blue.withOpacity(0.2)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: Colors.blue[200]?.withOpacity(0.5),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.class_outlined, color: Colors.blue, size: 24),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'PRÓXIMA CLASE',
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w900,
                          color: Colors.blue,
                          letterSpacing: 1.2,
                        ),
                      ),
                      Text(
                        _proximaClase['materia'],
                        style: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.bold,
                          color: Color(0xFF0D47A1),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const Divider(height: 24, color: Colors.blueAccent),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    const Icon(Icons.room, size: 16, color: Colors.blueGrey),
                    const SizedBox(width: 4),
                    Text(
                      _proximaClase['aula'],
                      style: const TextStyle(fontWeight: FontWeight.w500, color: Colors.blueGrey),
                    ),
                  ],
                ),
                Row(
                  children: [
                    const Icon(Icons.access_time_filled, size: 16, color: Colors.blueGrey),
                    const SizedBox(width: 4),
                    Text(
                      _proximaClase['horario'],
                      style: const TextStyle(fontWeight: FontWeight.w600, color: Colors.blueGrey),
                    ),
                  ],
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  /// Construye el indicador visual de Planificación del día (Tema hoy)
  Widget _buildTemaDelDiaCard(ColorScheme colorScheme, ThemeData theme) {
    return Card(
      elevation: 0,
      color: Colors.green[50] ?? const Color(0xFFE8F5E9), // Pastel verde ecológico/aprobado
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(color: Colors.green.withOpacity(0.2)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: Colors.green[200]?.withOpacity(0.5),
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(Icons.menu_book, color: Colors.green, size: 24),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'TEMA PLANIFICADO DE HOY',
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w900,
                          color: Colors.green,
                          letterSpacing: 1.2,
                        ),
                      ),
                      Text(
                        'Clase ${_planificacionHoy.nroClase}: ${_planificacionHoy.contenido}',
                        style: const TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.bold,
                          color: Color(0xFF1B5E20),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              'Unidad: ${_planificacionHoy.unidad}',
              style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: Colors.blueGrey),
            ),
            const SizedBox(height: 6),
            Text(
              'Actividades propuestas: ${_planificacionHoy.actividades}',
              style: TextStyle(fontSize: 13, color: Colors.grey[800]),
            ),
          ],
        ),
      ),
    );
  }

  /// Panel de Alertas Tempranas (Alumnos en lista filtrada en base a criterios de riesgo)
  Widget _buildAlertasTempranasPanel(ColorScheme colorScheme, ThemeData theme) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Row(
              children: [
                const Icon(Icons.warning_amber_rounded, color: Colors.redAccent),
                const SizedBox(width: 8),
                Text(
                  'Alertas de Alumnos',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: Colors.red[800],
                  ),
                ),
              ],
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, py: 4),
              decoration: BoxDecoration(
                color: Colors.red[100],
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(
                '${_alumnosConAlerta.length} Alumnos',
                style: TextStyle(
                  color: Colors.red[900],
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 10),
        Text(
          'Alumnos bajo riesgo: supera el 15% de faltas o promedio inferior a 6.0',
          style: TextStyle(fontSize: 12, color: Colors.grey[600]),
        ),
        const SizedBox(height: 12),
        
        // Lista vertical de tarjetas (vistas agile que evitan planillas infinitas)
        ListView.separated(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          itemCount: _alumnosConAlerta.length,
          separatorBuilder: (_, __) => const SizedBox(height: 10),
          itemBuilder: (context, index) {
            final alumno = _alumnosConAlerta[index];
            return Card(
              elevation: 0,
              color: Colors.white,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
                side: BorderSide(color: Colors.grey.withOpacity(0.2)),
              ),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
                child: Row(
                  children: [
                    CircleAvatar(
                      backgroundColor: Colors.red[50],
                      child: Text(
                        alumno.nombre.substring(0, 1),
                        style: TextStyle(color: Colors.red[900], fontWeight: FontWeight.bold),
                      ),
                    ),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            alumno.nombre,
                            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                          ),
                          const SizedBox(height: 4),
                          Row(
                            children: [
                              _buildBadge(
                                'Faltas: ${alumno.porcentajeFaltas.toStringAsFixed(0)}%',
                                alumno.porcentajeFaltas > 15 ? Colors.red[100]! : Colors.grey[200]!,
                                alumno.porcentajeFaltas > 15 ? Colors.red[900]! : Colors.black80,
                              ),
                              const SizedBox(width: 8),
                              _buildBadge(
                                'Promedio: ${alumno.promedioNotas.toStringAsFixed(2)}',
                                alumno.promedioNotas < 6 ? Colors.orange[100]! : Colors.green[100]!,
                                alumno.promedioNotas < 6 ? Colors.orange[900]! : Colors.green[900]!,
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.chevron_right, color: Colors.grey),
                      onPressed: () {
                        // Ir al historial/contacto del alumno
                      },
                    ),
                  ],
                ),
              ),
            );
          },
        ),
      ],
    );
  }

  Widget _buildBadge(String label, Color bg, Color text) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        label,
        style: TextStyle(color: text, fontSize: 11, fontWeight: FontWeight.bold),
      ),
    );
  }
}
