QSYSPREREG Pre-registration through Internet for QMS System


1. Install qsysprereg.war into Flassfish4.1.
2. Set preferences.

You must have system properties in Glassfish:
ххх_QSYSPREREG_CAPTION - заглавный текст на странице регистрации
ххх_QSYSPREREG_TITLE - the title of window
ххх_QSYSTEM_SERVER_ADDR - address of QMS QSystem
ххх_QSYSTEM_SERVER_PORT  - port of QMS QSystem
ххх_QSYSPREREG_LOGO - URL for logo image
xxx_QSYSPREREG_MAIL_CONTENT - full path to file with content for mail confirmation
ххх - this is value of parameter com from URL, unique for each branch

Set QSYSPREREG-MAIL for mailing.

3. Enjoy.



URL for app as wizard under Glassfish:
http://mysite.com:8080/qsysprereg/?com=XXX[&locale=yy_ZZ]


URL for app into iframe as one-page under Glassfish:
<iframe src="http://dev.apertum.ru:8080/qsysprereg/onepage.zul?com=demo[&locale=yy_ZZ]" frameborder="0" width="670" height="500"></iframe>