#!/bin/sh
npm run build_styles

mkdir -p ./build/resources/img
cp ./resources/img/* ./build/resources/img/

mkdir -p ./build/resources/fonts
cp ./resources/fonts/* ./build/resources/fonts/
cp ./node_modules/material-icons/iconfont/*.woff ./build/resources/fonts/
cp ./node_modules/material-icons/iconfont/*.woff2 ./build/resources/fonts/

cp ./templates/* ./build/
