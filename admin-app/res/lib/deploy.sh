ssh applyadmin.prod "mkdir /u/apps/apply-admin/web-${1}"
scp -r [!.]* applyadmin.prod:/u/apps/apply-admin/web-${1}
ssh applyadmin.prod "rm /u/apps/apply-admin/web"
ssh applyadmin.prod "ln -s /u/apps/apply-admin/web-${1} /u/apps/apply-admin/web"
