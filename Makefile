# ==============================================================================
# LuckyWheel - Java Minecraft Plugin
# Copyright (c) 2026 Daperkz
#
# Makefile
# ==============================================================================

PROJECT_NAME = LuckyWheel

all: build

build:
	mvn clean package
	@echo "The file .jar was generated in /target directory"

clean:
	mvn clean
	@echo "Projet clean."

re: clean build

.PHONY: build clean re
