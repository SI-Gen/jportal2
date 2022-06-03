echo Running JPortal2 from ${PWD}
docker run --rm -v ${PWD}:/local ghcr.io/si-gen/jportal2:1.8.14 \
                      --inputdir=/local/src/interfaces \
                      --template-location=/local/downloaded_jportal_templates \
                      --template-generator \
                        SQLAlchemy:/local/src/python/bbdcontent/sqlalchemy \
                      --builtin-generator \
                      PostgresDDL:/local/database/generated_sql \
                      --download-template-location=/local/downloaded_jportal_templates \
                      --download-template "SQLAlchemy:https://github.com/SI-Gen/jportal2-generator-vanguard-sqlalchemy/archive/refs/tags/1.3.zip|stripBaseDir"
