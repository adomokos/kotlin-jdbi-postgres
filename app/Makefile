mkfile_path := $(abspath $(lastword $(MAKEFILE_LIST)))

#################
# Gradle targets
#################

.PHONY: build
build: ## Build with Gradle
	./gradlew build -x check --warning-mode all

.PHONY: test
test: ## Runs the tests locally
	./gradlew test

.PHONY: check
check: ## Runs the Gradle checks locally
	./gradlew check --info

.PHONY: single-test
single-test: ## Run a sigle test, pass TEST=something.MySpec to run it
	 @test $(TEST) || (echo "TEST argument is required" ; exit 1)
	./gradlew :dataaccess:test --tests $(TEST)

.PHONY: clean
clean: ## Cleans all artifacts
	./gradlew clean

.PHONY: run
run: ## Runs the app
	./gradlew :cli:run

.PHONY: detekt
detekt: ## Calculates code complexity, checks styles
	./gradlew detekt

.PHONY: lint
lint: ## Uses ktlint to lint the Kotlin source code
	./gradlew ktlintCheck

.PHONY: format
format: ## Uses ktlint to format the Kotlin source code
	./gradlew ktlintFormat

.PHONY: update-check
update-check: ## Checks for updates for used libraries/plugins
	./gradlew dependencyUpdates -Drevision=release


.PHONY: help
help: ## Display this message
	@grep -h -E '^[a-zA-Z0-9\._-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
.DEFAULT_GOAL := help
