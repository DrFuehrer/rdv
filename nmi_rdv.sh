#!/bin/sh
# A simple wrapper script to debug and manage the environment
# $LastChangedDate$
# $LastChangedRevision$
# $LastChangedBy$
# $HeadURL$

MYPWD=$(pwd)
export RBNB_HOME=${MYPWD}/RBNB_2.5
echo "RBNB_HOME: ${RBNB_HOME}"
ant -buildfile trunk/build.xml clean jar
