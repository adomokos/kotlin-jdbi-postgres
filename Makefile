-include .env.$(or $(APP_ENV),dev)
export

APP_ENV ?=dev

mkfile_path := $(abspath $(lastword $(MAKEFILE_LIST)))
current_dir := $(notdir $(patsubst %/,%,$(dir $(mkfile_path))))

.PHONY: idea-launcher
idea-launcher: ## Launches IntelliJ IDEA with the provided env's .ENV file
	./idea-launcher

help: ## Prints this help message
	@grep -h -E '^[a-zA-Z0-9\._-]+:.*?## .*$$' $(MAKEFILE_LIST) |\
		sort | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
.DEFAULT_GOAL := help
