# Spec Delta

## Purpose
Define la página web pública del proyecto: qué cuenta, en qué idiomas, cómo muestra la app con capturas y qué garantías de accesibilidad, privacidad y publicación cumple.

## ADDED Requirements

### Requirement: Contenido de la página
La página SHALL explicar qué es Smart Bike y para qué sirve, mostrar el diagrama del setup, presentar cada funcionalidad principal (marchas virtuales, rodillo y mando para Zwift, pulso y ventilador, pantalla de rodaje y diagnóstico) con una captura de la app, indicar los límites del proyecto (hecho para un equipo concreto, solo Android, sin garantías, protocolos no oficiales), incluir el aviso de marcas y enlazar al repositorio y a la licencia.

#### Scenario: Primera visita
- **WHEN** una persona abre la página por primera vez
- **THEN** ve sin desplazarse qué es el proyecto y una captura de la pantalla de rodaje, y puede llegar con un clic al repositorio

#### Scenario: Límites visibles
- **WHEN** alguien lee la página hasta el final
- **THEN** encuentra que el proyecto está hecho para un equipo concreto, que solo existe para Android y que no tiene garantías, y el aviso de que las marcas pertenecen a sus propietarios

### Requirement: Español e inglés
La página SHALL existir completa en español y en inglés, con un selector de idioma visible en toda la página, y SHALL declarar el idioma de cada versión. La dirección raíz SHALL llevar a la versión que corresponda al idioma del navegador, o a la española si no es ninguno de los dos.

#### Scenario: Navegador en inglés
- **WHEN** se abre la dirección raíz con el navegador en inglés
- **THEN** se muestra la versión inglesa

#### Scenario: Cambiar de idioma
- **WHEN** el visitante elige el otro idioma en el selector
- **THEN** pasa a la misma sección de la otra versión, con el mismo contenido

#### Scenario: Idioma no admitido
- **WHEN** se abre la raíz con el navegador en otro idioma
- **THEN** se muestra la versión española

### Requirement: Capturas de las maquetas
Cada captura SHALL proceder de una maqueta del proyecto, con sus datos ilustrativos y sin la barra de revisión, dentro de un marco de móvil, y SHALL existir en tema oscuro y en tema claro. La página SHALL mostrar la captura del tema activo. Ninguna captura SHALL contener datos personales ni reales de dispositivos o de red.

#### Scenario: Captura del tema activo
- **WHEN** la página se muestra en tema claro
- **THEN** todas las capturas se ven en tema claro

#### Scenario: Descripción de las capturas
- **WHEN** una captura no se puede ver o la lee un lector de pantalla
- **THEN** su texto alternativo describe la pantalla y el estado que muestra

### Requirement: Tema y diseño
La página SHALL usar los colores, tipografías y espaciados del sistema de diseño del proyecto, seguir por defecto el tema claro u oscuro del sistema del visitante y permitir cambiarlo, recordando la elección en ese navegador.

#### Scenario: Sigue al sistema
- **WHEN** un visitante con el sistema en oscuro abre la página sin haber elegido tema
- **THEN** se muestra en oscuro

#### Scenario: Elección recordada
- **WHEN** el visitante cambia el tema y vuelve a abrir la página
- **THEN** se muestra con el tema que eligió

### Requirement: Adaptable y accesible
La página SHALL verse completa y sin desplazamiento horizontal desde 360 px de ancho hasta pantallas de escritorio, cumplir el contraste de texto de 4,5:1 en los dos temas, poder recorrerse con el teclado con el foco visible, usar una estructura de encabezados ordenada y respetar la preferencia de reducir movimiento.

#### Scenario: Móvil estrecho
- **WHEN** se abre la página en un ancho de 360 px
- **THEN** ninguna sección obliga a desplazarse en horizontal y los textos y los botones se leen y se pulsan con comodidad

#### Scenario: Solo teclado
- **WHEN** se recorre la página con la tecla de tabulación
- **THEN** el foco pasa por los enlaces y controles en orden y siempre es visible

### Requirement: Privacidad y autonomía
La página SHALL funcionar sin JavaScript para leer todo su contenido, SHALL NOT hacer peticiones a servicios externos (fuentes, analítica, redes sociales ni cookies de terceros) y SHALL NOT mostrar datos personales.

#### Scenario: Sin JavaScript
- **WHEN** se abre la página con JavaScript desactivado
- **THEN** se lee todo el contenido, con el tema del sistema y los enlaces entre idiomas

#### Scenario: Sin terceros
- **WHEN** se abre la página con el registro de red del navegador
- **THEN** todas las peticiones van al propio dominio de la página, salvo los enlaces que el visitante pulsa

### Requirement: Publicación
La página SHALL publicarse en GitHub Pages desde el repositorio, en la dirección `https://indoor-cycling-lab.github.io/smart-bike/`, cada vez que cambie su contenido en la rama principal, y SHALL publicar solo el contenido de la web, no el resto del repositorio.

#### Scenario: Cambio publicado
- **WHEN** se sube a la rama principal un cambio en el contenido de la web
- **THEN** la dirección pública muestra el cambio sin pasos manuales

#### Scenario: Solo la web
- **WHEN** se pide en la dirección pública un fichero de documentación interna del repositorio
- **THEN** no existe
