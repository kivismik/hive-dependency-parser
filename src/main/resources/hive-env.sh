#!/usr/bin/env bash

export HADOOP_HOME=${HADOOP_HOME:-/usr/lib/hadoop}

export HIVE_HOME=/usr/lib/hive

bin=/usr/lib/hive/bin

. "$bin"/hive-config.sh

if [ -f "${HIVE_CONF_DIR}/hive-env.sh" ]; then
  . "${HIVE_CONF_DIR}/hive-env.sh"
fi

CLASSPATH="${HIVE_CONF_DIR}"

HIVE_LIB=${HIVE_HOME}/lib

# needed for execution
if [ ! -f ${HIVE_LIB}/hive-exec-*.jar ]; then
  echo "Missing Hive Execution Jar: ${HIVE_LIB}/hive-exec-*.jar"
  exit 1;
fi

if [ ! -f ${HIVE_LIB}/hive-builtins-*.jar ]; then
  echo "Missing Hive Builtins Jar: ${HIVE_LIB}/hive-builtins-*.jar"
  exit 1;
fi

if [ ! -f ${HIVE_LIB}/hive-metastore-*.jar ]; then
  echo "Missing Hive MetaStore Jar"
  exit 2;
fi

# cli specific code
if [ ! -f ${HIVE_LIB}/hive-cli-*.jar ]; then
  echo "Missing Hive CLI Jar"
  exit 3;
fi

for f in ${HIVE_LIB}/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done

# add the auxillary jars such as serdes
if [ -d "${HIVE_AUX_JARS_PATH}" ]; then
  for f in ${HIVE_AUX_JARS_PATH}/*.jar; do
    if [[ ! -f $f ]]; then
        continue;
    fi
    AUX_CLASSPATH=${AUX_CLASSPATH}:$f
  done
elif [ "${HIVE_AUX_JARS_PATH}" != "" ]; then 
  AUX_CLASSPATH=${HIVE_AUX_JARS_PATH}
fi

# adding jars from auxlib directory
for f in ${HIVE_HOME}/auxlib/*.jar; do
  if [[ ! -f $f ]]; then
      continue;
  fi
  AUX_CLASSPATH=${AUX_CLASSPATH}:$f
done
CLASSPATH=${CLASSPATH}:${AUX_CLASSPATH}

CLASSPATH=${CLASSPATH}:`hadoop classpath`

CLASSPATH=${CLASSPATH}:`pwd`/hive-parse-test-1.0.jar

export CLASSPATH
