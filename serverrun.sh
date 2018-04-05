git pull
rm out/server/*
rm out/server/tetrispackage/*
cd src/main/java/tetrispackage
javac Server*.java -d ../../../../out/server
cd ../../../../out/server
java -cp ~/tetris/out/mysql-connector-java-5.1.46/mysql-connector-java-5.1.46-bin.jar:. tetrispackage.ServerMain
