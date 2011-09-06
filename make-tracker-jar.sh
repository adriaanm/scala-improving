#!/usr/bin/env bash
#

set -e

root=$(pwd)
dir=$(mktemp -dt tracker)

cd target/scala-2.10.0-SNAPSHOT/classes
rsync -aRv improving/memory ${dir}/

cd "$dir"
mkdir META-INF
cat > META-INF/MANIFEST.MF <<EOM
Premain-Class: improving.memory.TrackMemory
Main-Class: scala.tools.nsc.MainGenericRunner
Manifest-Version: 1.0
Created-By: 1.6.0_26 (Apple Inc.)
EOM

jar cmf META-INF/MANIFEST.MF track-memory.jar *
cp *.jar "$root"/libs
cd "$root"

echo scala -J-javaagent:libs/track-memory.jar
