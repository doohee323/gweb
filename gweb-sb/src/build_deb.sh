cp -rf target/gweb-sb-1.0.0-jar-with-dependencies.jar src/gweb-sb/usr/local/etc/gweb-sb
cp -rf src/main/resources/*.properties src/gweb-sb/usr/local/etc/gweb-sb
cp -rf src/main/resources/*.conf src/gweb-sb/usr/local/etc/gweb-sb
cp -rf src/main/resources/*.xml src/gweb-sb/usr/local/etc/gweb-sb

chmod 775 gweb-sb/DEBIAN/postinst

dpkg -b gweb-sb
