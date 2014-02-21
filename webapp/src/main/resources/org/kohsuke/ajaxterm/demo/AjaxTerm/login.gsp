<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
	<title>Ajaxterm4j</title>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
</head>
<body>
  <h2>Start shell locally</h2>
  <form method="post" action="local">
    <input type=hidden name=w value=80>
    <input type=hidden name=h value=25>
    <input type=submit value=Start>
  </form>

  <h2>Login to remote host via SSH</h2>
  <form method="post" action="login">
    <div>
      Host: <input type=text name=host>
    </div>
    <div>
      Port: <input type=text name=port value=22>
    </div>
    <div>
      User: <input type=text name=user>
    </div>
    <div>
      Password: <input type=password name=password>
    </div>
    <input type=hidden name=w value=80>
    <input type=hidden name=h value=25>
    <input type=submit value=Login>
  </form>
</body>
</html>
