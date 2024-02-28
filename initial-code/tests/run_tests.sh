################################################
### PATHS (feel free to tweak paths accordingly)
CLI_PATH=${PWD}/../Client
TESTS_FOLDER=${PWD}
TESTS_OUT_EXPECTED=${TESTS_FOLDER}/expected
TESTS_OUTPUT=${TESTS_FOLDER}/test-outputs
################################################
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'
################################################

rm -rf $TESTS_OUTPUT
mkdir -p $TESTS_OUTPUT

cd $CLI_PATH
for i in {1..5}
do
    TEST=$(printf "%02d" $i)
    mvn --quiet compile exec:java < ${TESTS_FOLDER}/input$TEST.txt > ${TESTS_OUTPUT}/out$TEST.txt

    DIFF=$(diff ${TESTS_OUTPUT}/out$TEST.txt ${TESTS_OUT_EXPECTED}/out$TEST.txt) 
    if [ "$DIFF" != "" ] 
    then
        echo "${RED}[$TEST] TEST FAILED${NC}"
    else
        echo "${GREEN}[$TEST] TEST PASSED${NC}"
    fi
done

echo "Check the outputs of each test in ${TESTS_OUTPUT}."
