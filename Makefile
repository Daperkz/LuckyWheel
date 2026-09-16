# ==============================================================================
# LuckyWheel - Java Minecraft Plugin
# Copyright (c) 2026 Daperkz
#
# Makefile
# ==============================================================================

PROJECT_NAME 	= LuckyWheel

GRADLEW 		= ./gradlew
RM				=	rm
RMFLAGS			=	-rf

JAVA_VERSION	?= 25
JDK_DIR 		= $(CURDIR)/.tools/jdk-$(JAVA_VERSION)

PAPER_VERSIONS	?=
PAPER_ARGS 		= $(if $(PAPER_VERSIONS),-PpaperVersions=$(PAPER_VERSIONS),)

GRADLE_JAVA_ARGS = -Dorg.gradle.java.installations.paths=$(CURDIR)/.tools/jdk-$(JAVA_VERSION)

all: jar

build: bootstrap-jdk
	@JAVA_HOME="$(JDK_DIR)" PATH="$(JDK_DIR)/bin:$$PATH" $(GRADLEW) $(GRADLE_JAVA_ARGS) compileAllVersions -PjavaVersion=$(JAVA_VERSION) $(PAPER_ARGS) $(GRADLE_ARGS)

jar: bootstrap-jdk
	@JAVA_HOME="$(JDK_DIR)" PATH="$(JDK_DIR)/bin:$$PATH" $(GRADLEW) $(GRADLE_JAVA_ARGS) buildAllVersions -PjavaVersion=$(JAVA_VERSION) $(PAPER_ARGS) $(GRADLE_ARGS)
	@echo "Versioned JARs are in build/libs/."

bootstrap-jdk:
	@set -eu; \
	jdk_dir=".tools/jdk-$(JAVA_VERSION)"; \
	if command -v javac >/dev/null 2>&1 && [ "$$(javac -version 2>&1 | awk '{print $$2}' | cut -d. -f1)" = "$(JAVA_VERSION)" ]; then \
		javac_path="$$(command -v javac)"; \
		system_jdk="$$(dirname "$$(dirname "$$(readlink -f "$$javac_path")")")"; \
		mkdir -p .tools; \
		rm -rf "$$jdk_dir"; \
		ln -s "$$system_jdk" "$$jdk_dir"; \
		echo "Using system JDK $(JAVA_VERSION) at $$system_jdk."; \
	elif [ -x "$$jdk_dir/bin/javac" ]; then \
		echo "Using project JDK at $$jdk_dir."; \
	else \
		echo "JDK $(JAVA_VERSION) not found; downloading Eclipse Temurin..."; \
		command -v curl >/dev/null 2>&1 || { echo "Error: curl is required." >&2; exit 1; }; \
		command -v tar >/dev/null 2>&1 || { echo "Error: tar is required." >&2; exit 1; }; \
		mkdir -p .tools; \
		tmp_dir="$$(mktemp -d .tools/jdk-download.XXXXXX)"; \
		trap 'rm -rf "$$tmp_dir"' EXIT; \
		curl -fL "https://api.adoptium.net/v3/binary/latest/$(JAVA_VERSION)/ga/linux/x64/jdk/hotspot/normal/eclipse" -o "$$tmp_dir/jdk.tar.gz"; \
		tar -xzf "$$tmp_dir/jdk.tar.gz" -C "$$tmp_dir"; \
		extracted_dir="$$(find "$$tmp_dir" -mindepth 1 -maxdepth 1 -type d -print -quit)"; \
		[ -n "$$extracted_dir" ] || { echo "Error: unexpected JDK archive layout." >&2; exit 1; }; \
		rm -rf "$$jdk_dir"; \
		mv "$$extracted_dir" "$$jdk_dir"; \
		echo "Installed project JDK at $$jdk_dir."; \
	fi

clean: bootstrap-jdk
	@JAVA_HOME="$(JDK_DIR)" PATH="$(JDK_DIR)/bin:$$PATH" $(GRADLEW) $(GRADLE_JAVA_ARGS) clean -PjavaVersion=$(JAVA_VERSION)
	$(RM) $(RMFLAGS) .tools/

re: clean jar

.PHONY: all build jar bootstrap-jdk clean re
