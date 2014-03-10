You will want to download javax.mail.jar and the PrintJobWatcher class to go with this.
Then you can compile the project with:
javac -cp javax.mail.jar:. main.java
jar -cmf Manifest.txt Word++.jar *.class *.png