mysqldump --host=localhost --user=root --password=root --single-transaction --complete-insert --no-create-info --no-create-db --column-statistics=0 confservice -t app -t benchmark -t catalog_app -t guarantee -t infrastructure -t infrastructure_provider -t jhi_authority -t jhi_user -t jhi_user_authority -t node -t project -t service_provider -t sla -t sla_violation -t app_constraint rec