export GPG_TTY=$(tty)
mvn -P release clean deploy -DskipTests