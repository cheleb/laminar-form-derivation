pushd examples/client
rm -rf package-lock.json
npm i
npm run build
popd
export VERSION=`git describe --tags --abbrev=0 | sed "s/v//"`
echo "Documentation version: $VERSION"
sbt website