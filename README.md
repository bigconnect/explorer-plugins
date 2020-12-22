<p align="center">
  <img src="https://github.com/bigconnect/bigconnect/raw/master/docs/logo.png" alt="BigConnect Logo"/>
  <br>
  The multi-model Big Graph Store<br>
</p>

# BigConnect Explorer Plugins

Explorer plugins provide a way to extend or alter the features of BigConnect Explorer.

This repository contains plugins for various features:

* **auth-username-only** - Login to Explorer without providing a password 
* **face-recognition** - Receive Face Recognition events from an external program  

## Installation
Build and install the plugins using Maven:
```
mvn clean install
```

## Installing plugins
In order to install a Data Processing plugin just copy the jar file of the plugin to the ```lib/``` folder of
[BigConnect Core](https://github.com/bigconnect/bigconnect) or BigConnect Explorer

## Contributing
Contributions are warmly welcomed and greatly appreciated. Here are a few ways you can contribute:

* Start by fixing some [issues](https://github.com/bigconnect/bigconnect/issues?q=is%3Aissue+is%3Aopen)
* Submit a Pull Request with your fix

## Getting help & Contact
* [Official Forum](https://community.bigconnect.io/)
* [LinkedIn Page](https://www.linkedin.com/company/bigconnectcloud/)

## License
BigConnect is an open source product licensed under AGPLv3.
