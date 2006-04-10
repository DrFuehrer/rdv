#! /bin/sh
args=`getopt k:j: $*`
USAGE="$0 -k <path/to/keystore> -j <path/to/jar/to/be/signed>"
set -- $args

for i do
  case "$i" in
    -k)
	  KEYSTORE="$2"; shift;
	  shift;;
    -j)
	  JAR_LOC="$2"; shift;
	  shift;;
    --)
	  shift; break;;
  esac
done

if [ "${JAR_LOC}" -a "${KEYSTORE}" ]; then
    echo "jarsigner -keystore ${KEYSTORE} ${JAR_LOC} NEESitCert"
else
  echo $USAGE;
  exit 2;
fi

