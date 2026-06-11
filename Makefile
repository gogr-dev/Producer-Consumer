JAVAC := javac
JAVA  := java
SRC   := $(wildcard src/pc/*.java)
OUT   := out

# Pass arguments to `make run`, e.g.:  make run ARGS="--seconds 5 --impl lock"
ARGS ?=

.PHONY: all run test clean

all: $(OUT)/.compiled

$(OUT)/.compiled: $(SRC)
	@mkdir -p $(OUT)
	$(JAVAC) -d $(OUT) $(SRC)
	@touch $(OUT)/.compiled

run: all
	$(JAVA) -cp $(OUT) pc.ProducerConsumer $(ARGS)

test: all
	$(JAVA) -cp $(OUT) pc.BufferTest

clean:
	rm -rf $(OUT)
