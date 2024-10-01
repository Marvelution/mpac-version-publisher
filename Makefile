ifdef MVN_FLAGS
    $(info MVN_FLAGS defined manually)
else
    MVN_FLAGS := "-e"
endif


.SILENT:

build: build-parent build-core build-cli
	echo "finished build"

build-parent:
	./mvnw $(MVN_FLAGS) -DskipTests clean install

build-cli:
	./mvnw $(MVN_FLAGS) -DskipTests -pl cli clean install

build-core:
	./mvnw $(MVN_FLAGS) -DskipTests -pl core clean install

test: test-core test-cli
	echo "finished testing"

test-cli:
	./mvnw $(MVN_FLAGS) -pl cli test

test-core:
	./mvnw $(MVN_FLAGS) -pl core test
