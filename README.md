# 🍽️ Aplicación de Gestión de Pedidos - Restaurante "El Porvenir Steaks"

## Descripción del Proyecto

Aplicación móvil desarrollada en Android Studio para la gestión de pedidos del restaurante "El Porvenir Steaks". El sistema permite a los clientes realizar pedidos, rastrear sus entregas en tiempo real, y al restaurante administrar todo el proceso desde la recepción hasta la entrega.

Este proyecto es desarrollado como parte de la asignatura **PMO-0602 Programación Móvil I** en la carrera de Ingeniería en Computación - UTH.

## 🚀 Tecnologías

- **Frontend**: Android Studio (Java)
- **Backend**: Laravel 12 (PHP) para API REST
- **Base de Datos**: MySQL y Firebase Realtime Database
- **Hosting**: AWS (EC2)
- **Notificaciones**: Firebase Cloud Messaging (Revision)
- **Geolocalización**: Google Maps API (Revision)
- **Autenticación**: Sanctum + Firebase Auth (Revision)

## 🏗️ Arquitectura

La aplicación sigue una arquitectura cliente-servidor con:
- Aplicación Android para clientes y repartidores
- API RESTful desarrollada en Laravel
- Servicios en tiempo real con Firebase
- Base de datos

## 🌟 Características

### Para Clientes
- Registro y autenticación con verificación por correo
- Recuperación de contraseña
- Catálogo de productos del restaurante
- Creación y seguimiento de pedidos
- Geolocalización para entrega
- Notificaciones push de estado del pedido
- Calificación del servicio

### Para Repartidores
- Vista de pedidos asignados
- Acceso a datos del cliente y ubicación
- Confirmación de entregas

### Para Administradores
- Panel de pedidos activos y completados
- Gestión de productos
- Asignación de repartidores

## 📂 Estructura del Repositorio

Este repositorio sigue una estructura basada en GitFlow para facilitar el desarrollo colaborativo:

### Ramas Principales

- **`main`**: Código estable de producción. Protegido contra commits directos.
- **`staging`**: Entorno de pre-producción para pruebas integradas.
- **`development`**: Rama principal de desarrollo donde se integran las funcionalidades.

### Ramas de Funcionalidades

Todas las ramas de funcionalidades se crean a partir de `development`:

```
git checkout development
git checkout -b feature/nombre-de-funcionalidad
```

### Ramas de Corrección de Errores

Para corregir bugs:

```
git checkout development
git checkout -b bugfix/descripcion-del-error
```

## 🔄 Flujo de Trabajo Git

1. **Crear rama de funcionalidad desde development**
   ```
   git checkout development
   git pull
   git checkout -b feature/tu-funcionalidad
   ```

2. **Desarrollo y commits en la rama**
   ```
   git add .
   git commit -m "Descripción clara del cambio"
   git push origin feature/tu-funcionalidad
   ```

3. **Mantener la rama actualizada**
   ```
   git checkout development
   git pull
   git checkout feature/tu-funcionalidad
   git merge development
   ```

4. **Crear Pull Request**
   - Crea un PR en GitHub de tu rama hacia `development`
   - Asigna revisores
   - Espera aprobación

5. **Después del merge**
   ```
   git checkout development
   git pull
   git branch -d feature/tu-funcionalidad
   ```

## 📋 Reglas del Equipo

1. **No commits directos a main o staging**
2. **Pull requests obligatorios** para todas las integraciones
3. **Revisión de código** requerida antes de aprobar PR
4. **Comentarios descriptivos** en los commits
5. **Mantener las ramas actualizadas** regularmente
6. **Tests unitarios** para nuevas funcionalidades

## 🚀 Configuración del Proyecto

### Requisitos Previos
- Android Studio 4.0+
- Kotlin 1.5+
- PHP 8.1+
- Composer
- Laravel 12
- Git

### Configuración Inicial
1. Clonar el repositorio
   ```
   git clone [URL_DEL_REPOSITORIO]
   ```
2. Abrir el proyecto en Android Studio
3. Sincronizar Gradle
4. Configurar Firebase (seguir instrucciones en `/docs/firebase-setup.md`)
5. Configurar Laravel (seguir instrucciones en `/docs/laravel-setup.md`)

## 📝 Tareas Pendientes

- [x] Configuración inicial del proyecto Android
- [ ] Diseño de la base de datos
- [ ] Implementación del sistema de autenticación
- [ ] Desarrollo de API REST en Laravel
- [ ] Integración con Firebase
- [ ] Implementación de geolocalización
- [ ] Sistema de notificaciones push
- [ ] Testing y depuración

## 👥 Equipo

- Equipo 1

## 📄 Licencia

Este proyecto es parte de un trabajo académico.

---
