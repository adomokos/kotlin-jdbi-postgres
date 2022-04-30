-include ../.env.$(or $(APP_ENV),dev)
export

APP_ENV ?=dev

mkfile_path := $(abspath $(lastword $(MAKEFILE_LIST)))
current_dir := $(notdir $(patsubst %/,%,$(dir $(mkfile_path))))

.PHONY: db-reset
db-reset: ## Stops and resets the DB by recreating it with migrations
	@docker container kill $(shell docker container ls -q --filter name=jdbiktpg-db-) \
		 | grep -q . && echo DB container killed || echo DB container not found
	@docker container rm $(shell docker container ls -q --filter name=jdbiktpg-db-) \
		 | grep -q . && echo DB container removed || echo DB container not found
	docker-compose up -d

.PHONY: db-prompt
db-prompt: ## Jumps into the Postgres DB psql prompt
		PGPASSWORD='$(POSTGRES_PASSWORD)' PGHOST=$(POSTGRES_HOST) \
		 psql -U $(POSTGRES_USER) -d $(POSTGRES_DB)

.PHONY: db-migrate-info
db-migrate-info: ## Provides information of Postgres migrations
	flyway info -locations="flyway/sql" -schemas="jdbiktpg" -baselineOnMigrate="true" \
		 -url="jdbc:postgresql://$(POSTGRES_HOST):$(POSTGRES_PORT)/$(POSTGRES_DB)?user=$(POSTGRES_USER)&password=$(POSTGRES_PASSWORD)"

.PHONY: db-migrate
db-migrate: ## Runs Postgres migrations with Flyway
	flyway migrate -locations="flyway/sql" -schemas="jdbiktpg" -baselineOnMigrate="true" \
		 -url="jdbc:postgresql://$(POSTGRES_HOST):$(POSTGRES_PORT)/$(POSTGRES_DB)?user=$(POSTGRES_USER)&password=$(POSTGRES_PASSWORD)"

.PHONY: db-print
db-print: ## Prints tables populated from Codat
	$(MAKE) -f debugging.mk db-print

#######################
## Docker-based Targets
#######################

.PHONY: ci.db-reset
ci.db-reset: ## Stops and resets the DB by recreating it with migrations - on the CI Server
	docker-compose up -d jdbiktpg-db
	sleep 1
	$(MAKE) docker.db-migrate

.PHONY: docker.db-migrate
docker.db-migrate: ## Runs the migrations against Postgres with flyway
	docker run -t --rm --net="host" -v $(PWD)/flyway/sql:/flyway/sql \
		 flyway/flyway:latest-alpine migrate -baselineOnMigrate="true" \
		 -url="jdbc:postgresql://$(POSTGRES_HOST):$(POSTGRES_PORT)/$(POSTGRES_DB)?user=$(POSTGRES_USER)&password=$(POSTGRES_PASSWORD)" \
		 -schemas="jdbiktpg"

help: ## Prints this help message
	@grep -h -E '^[a-zA-Z0-9\._-]+:.*?## .*$$' $(MAKEFILE_LIST) |\
		sort | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
.DEFAULT_GOAL := help
