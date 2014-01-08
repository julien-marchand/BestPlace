		Entropy

Version: 1.2.0
Contact: http://entropy.gforge.inria.fr

What is it?
-----------

Entropy is a consolidation manager for clusters. It
provides a dynamic reassignment of virtual machines
that to use a minimum number of nodes, following
the requirements of the virtual machines.

Documentation
-------------

The complete documentation of Entropy is available 
online at http://entropy.gforge.inria.fr

Release Notes
-------------

The full list of changes can be found
at http://entropy.gforge.inria.fr/release-notes.html#1.0.0


System Requirements
-------------------

  * JDK - 1.5 or above (developed and tested with the 1.5 and 1.6)
  * Memory - 1GB
  * Disk - no minimum requirement
  * Operating System - Should be able to run on any system that provides
    	      	       a compatible JAVA environment. Tested on GNU/Linux.
		       Management scripts for GNU/Linux


Installation Instructions
-------------------------

  1) Entropy is a Java tool, so you must have Java
     installed in order to proceed.
  
  2) Extract the distribution into the future parent directory of entropy
     (e.g "/usr/local"). The distribution contains the following files:
     
---   
entropy-1.0.0
|-- LICENSE.txt
|-- README.txt
|-- bin
|   `-- controlLoop.sh
|-- config
|   |-- entropy.properties
|   `-- log4j.properties
|-- jar
|   |-- choco-1.2.04.jar
|   |-- entropy-1.0.0.jar
|   |-- jsch-0.1.38.jar
|   `-- log4j-1.2.9.jar
`-- logs
---
   
   * The "bin" directory contains management scripts.
     By default, scripts are made for GNU/Linux.   
   * The "config" directory contains configuration files.
     'entropy.properties' contains properties related to entropy,
     'log4j.properties>' contains instructions for the logging system .     
   * The "jar" directory contains the libraries used by Entropy.   
   * The "logs" is the default output directory for the logging system.
  
  For a detailled description of the configuration files and howto
  use Entropy, refer to http://entropy.gforge.inria.fr

Licensing
---------

Please see the file called LICENSE.TXT



