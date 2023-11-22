#! /bin/sh

export NODE_ENV=development
npx webpack --progress --mode development

cp -r dist/ ../../../../../../../../target/classes/io/bigconnect/web/actions/video/
cp *.less ../../../../../../../../target/classes/io/bigconnect/web/actions/video/
cp *.js ../../../../../../../../target/classes/io/bigconnect/web/actions/video/
cp worker/*.js ../../../../../../../../target/classes/io/bigconnect/web/actions/video/worker
