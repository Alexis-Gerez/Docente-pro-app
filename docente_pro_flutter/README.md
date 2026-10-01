# Arquitectura y Estructura de Carpetas - DocentePro App 🚀

Este documento detalla la estructura modular de carpetas propuesta para "DocentePro App", diseñada según los principios de **Clean Architecture** y la gestión de estado con un enfoque de arquitectura escalable, desacoplada y preparada para un funcionamiento **Offline-First**.

---

## 📂 Organización del Proyecto Flutter

```text
docente_pro_flutter/
├── android/                  # Archivos de configuración nativa de Android
├── ios/                      # Archivos de configuración nativa de iOS
├── web/                      # Soporte Web (si se requiere)
├── assets/
│   ├── images/               # Fotos de docentes, logos e imágenes por defecto
│   └── icons/                # Vectores SVG de estados e íconos personalizados
├── lib/
│   ├── main.dart             # Punto de entrada de la aplicación Flutter
│   │
│   ├── core/                 # Utilidades globales, temas, constantes y enrutamiento
│   │   ├── theme/            # Colores pasteles, fuentes y estilos (Modo Claro/Oscuro)
│   │   │   ├── colors.dart
│   │   │   └── typography.dart
│   │   ├── utils/            # Extensiones de Dart, formateadores y validadores
│   │   │   └── date_formatter.dart
│   │   ├── database/         # Motor de almacenamiento SQLite local (Hive/Isar/Room-equivalent)
│   │   │   └── local_db_client.dart
│   │   └── network/          # Cliente HTTP/Retrofit preparado para futuras conexiones (Firebase)
│   │       └── api_client.dart
│   │
│   ├── features/             # Características modulares por funcionalidad
│   │   │
│   │   ├── dashboard/        # Módulo 1: Inicio y Resumen del día (Dashboard)
│   │   │   ├── data/
│   │   │   │   ├── models/   # Representación interna de la UI
│   │   │   │   └── repositories/
│   │   │   ├── domain/
│   │   │   └── presentation/
│   │   │       ├── controllers/ # Gestores de estado (Riverpod / ViewModel Providers)
│   │   │       ├── screens/
│   │   │       │   └── dashboard_screen.dart # Dashboard principal entregado
│   │   │       └── widgets/  # Componentes internos (Próxima Clase, Alerta Temprana, etc.)
│   │   │
│   │   ├── planificacion/    # Módulo 2: Motor de Planificación Inteligente en Cascada
│   │   │   ├── data/
│   │   │   │   ├── datasources/ # Fuentes locales (Offline SharedPrefs & Hive boxes)
│   │   │   │   │   ├── planificacion_local_source.dart
│   │   │   │   │   └── interrupcion_local_source.dart
│   │   │   │   └── models/
│   │   │   │       ├── planificacion_dto.dart
│   │   │   │       └── docente_model.dart
│   │   │   ├── domain/
│   │   │   │   ├── entities/    # Modelos de dominio puros (Libres de dependencias de bases de datos)
│   │   │   │   │   ├── docente.dart
│   │   │   │   │   ├── materia.dart
│   │   │   │   │   └── planificacion_clase.dart
│   │   │   │   ├── usecases/   # Casos de uso de negocio autónomos
│   │   │   │   │   ├── reprogramar_clases_usecase.dart
│   │   │   │   │   └── registrar_interrupcion_usecase.dart
│   │   │   │   └── repositories/
│   │   │   │       └── planificacion_repository_interface.dart
│   │   │   └── presentation/
│   │   │       ├── provider/   # Lógica reactiva de control de planificación en cascada
│   │   │       │   └── planificacion_notifier.dart
│   │   │       ├── screens/
│   │   │       │   └── gestion_planificacion_screen.dart
│   │   │       └── widgets/
│   │   │           ├── calendar_selector_widget.dart
│   │   │           └── class_item_card.dart
│   │   │
│   │   ├── asistencia/       # Módulo 3: Presentismo Ágil por Gestos Swipe
│   │   │   ├── data/
│   │   │   │   └── models/   # Alumno model e historiales
│   │   │   ├── domain/
│   │   │   └── presentation/
│   │   │       ├── state/
│   │   │       │   └── asistencia_state_notifier.dart
│   │   │       ├── screens/
│   │   │       │   └── toma_asistencia_swipe_screen.dart
│   │   │       └── widgets/
│   │   │           ├── swipe_student_card.dart  # Tarjetas de alumnos con soporte Gestual Swipe
│   │   │           └── bulk_attendance_button.dart # Botón de asistencia masiva rápida
│   │   │
│   │   └── perfil/           # Módulo 4: Perfil del Docente (Múltiples Cargos/Escuelas)
│   │       ├── domain/
│   │       └── presentation/
│   │           └── screens/
│   │               └── docente_profile_screen.dart
│   │
│   └── test/                 # Carpeta de test unitarios e instrumentados
│       ├── unit_test/
│       │   └── planificacion_cascada_test.dart  # Tests para validar el algoritmo de cascada
│       └── widget_test/
```

---

## 🛡️ Fundamentos Arquitectónicos Aplicados

1. **Separación de Capas (Clean Architecture):**
   - **Domain (Capa de Reglas de Negocio):** Es la clase más interna de la aplicación. Contiene las Entidades (`docente.dart`, `plani.dart`, `alumno.dart`) y los Casos de Uso (`reprogramar_clases_usecase.dart`) y las firmas abstractas de Repositorios. No conoce bases de datos ni UI. Es 100% testeable en Dart clásico.
   - **Data (Capa de Infraestructura):** Implementa las interfaces de los repositorios. Maneja el origen físico de la información: `Hive` para caché local, `HttpClient` para peticiones y `FirebaseStore` en caso de online. Convierte los datos sin procesar (DTO / JSON) en entidades de domino mediante "Mappers".
   - **Presentation (Capa de Interfaz de Usuario):** Se encarga de pintar widgets elegantes en Flutter y emitir estados reactivos usando `Riverpod` o `Provider` a través de notificadores (`StateNotifier`).

2. **Estrategia Offline-First:**
   - La aplicación consulta inicialmente las tablas/cajas de almacenamiento local independientes. Cualquier cambio de asistencia gestual o reprogramación de clases se asienta localmente al instante en milisegundos.
   - Un mecanismo de sincronización sincroniza las bitácoras o transacciones diferidas con Firebase u otro backend del servidor cuando detecte conectividad activa de internet, protegiendo al docente de redes escolares inestables.
