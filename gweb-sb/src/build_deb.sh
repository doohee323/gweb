cd ..
mvn clean compile package

mkdir -p gweb-sb/usr/local/etc/gweb-sb

cp -rf target/gweb-sb-1.0.0-jar-with-dependencies.jar src/gweb-sb/usr/local/etc/gweb-sb
cp -rf main/resources/*.properties gweb-sb/usr/local/etc/gweb-sb
cp -rf main/resources/*.conf gweb-sb/usr/local/etc/gweb-sb
cp -rf main/resources/*.xml gweb-sb/usr/local/etc/gweb-sb

chmod 775 gweb-sb/DEBIAN/postinst

dpkg -b gweb-sb
