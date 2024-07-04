default:
   just --list

make-zip:
   #!/bin/bash
   rm -f qqq-app-starter.zip
   zip -r qqq-app-starter.zip README.md checkstyle pom.xml src

