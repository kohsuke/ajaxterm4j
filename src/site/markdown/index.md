What is Ajaxterm4j?
====

Ajaxterm4j is a Java port of [Ajaxterm](http://antony.lesuisse.org/software/ajaxterm/), which lets us emulate a terminal on a web browser.

Ajaxterm4j consists of JavaScript that renders the terminal on the client, `XmlHttpRequest` that sends keystrokes and screens back and forth,
and a Java library that launches/manages processes. The library plays a similar role to xterm, except that whereas xterm is
implemented in C and use X for rendering, ajaxterm4j is implemented in Java and uses Ajax/HTML for rendering.

Usage
---

Ajaxterm4j is defined in a JavaScript file and CSS. Include those in your page:

	<link rel="stylesheet" type="text/css" href="ajaxterm.css"/>
	<script type="text/javascript" src="ajaxterm.js"></script>

Ajaxterm4j creates a terminal inside a DIV element with the 'ajaxterm' as the CSS class name:

    <div id="myTestTerminal" class="ajaxterm"></div>

Finally, instantiate a terminal. The first parameter is the ID or the element of the DIV element.
The second argument is an option hash:

    new ajaxterm.Terminal("myTestTerminal",{width:80,height:25,endpoint:"path/to/somewhere"});

Glueing together the server-side is up to you.
Depending on the web framework of your choice, how you intergrate this with your webapp will be different.
Basically,

1. For each terminal session, a `Session` instance needs to be created.
   (multiple clients can connect to the same `Session` instance to create screencast-like effect.)
1. The URL specified in the `endpoint` parameter needs to call into `Session.handleUpdate`

Refer to the javadoc of the `Session` class for more details.

Demo application
---
The source tree contains a demo application that you can run with `mvn install; cd webapp; mvn jetty:run`