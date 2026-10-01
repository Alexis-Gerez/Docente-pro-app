Docente Pro App 📱
Aplicación Android para la gestión integral del aula: asistencias, calificaciones,planificación anual, mensual y diaria, cursos, materias y calendario académico —con uso pleno desde el celular.

Motivación
Nace de mi experiencia como docente técnico: las señales tempranas de deserción ybajo rendimiento (ausencias, prácticas pendientes, evaluaciones) hoy se pierden enplanillas dispersas. La app centraliza esos datos en un solo lugar para poderintervenir a tiempo.

Funcionalidades (MVP funcional)
✅ Registro de asistencias
✅ Calificaciones
✅ Planificación anual, mensual y diaria
✅ Cursos y materias
✅ Calendario académico
🚧 En desarrollo: importación y organización automática de planificacionesa partir de documentos, asistida por IA (Gemini API)

Rol
Diagnóstico del problema, especificación funcional, diseño de la experiencia de usoy validación iterativa. Desarrollo asistido con IA bajo mi dirección.

Para desarrolladores
Requisitos: Android Studio
Cloná este repo y abrilo con Android Studio (permití que resuelva dependencias)
Creá un archivo .env en la raíz con tu propia GEMINI_API_KEY(ver .env.example como referencia)
Remové la línea signingConfig = signingConfigs.getByName("debugConfig")del build.gradle.kts antes de compilar
Ejecutá en emulador o dispositivo físico
