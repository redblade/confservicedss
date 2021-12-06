jhipster version 6.10.5

#update src
cd ..
jhipster jdl jhipster-jdl.jdl

#remove unused folders
rm -fr src/main/resources/config/liquibase/
rm -fr src/test/javascript/

#UML
#install jdlbridge
npm install -g jdlbridge

#convert jdl in plantuml
jdlbridge -u -f jhipster-jdl.jdl

#add entity (eg. only client)
jhipster entity <entityName> --[options]
https://www.jhipster.tech/creating-an-entity/

#view online at http://www.plantuml.com/ and download png
