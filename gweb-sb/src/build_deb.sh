cd ..
mvn clean compile package

mkdir -p src/pkg-build/usr/local/etc/gweb-sb

cp -rf target/gweb-sb-1.0.0-jar-with-dependencies.jar src/pkg-build/usr/local/etc/gweb-sb
cp -rf src/main/resources/*.properties src/pkg-build/usr/local/etc/gweb-sb
cp -rf src/main/resources/*.conf src/pkg-build/usr/local/etc/gweb-sb
cp -rf src/main/resources/*.xml src/pkg-build/usr/local/etc/gweb-sb

chmod 775 src/pkg-build/DEBIAN/postinst

dpkg -b pkg-build
mv pkg-build.deb gweb-sb.deb
