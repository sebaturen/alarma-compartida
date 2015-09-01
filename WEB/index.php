<?php
	
	// include db connect class
	require_once __DIR__ . '/db_connect.php';
	// Function file
	require_once __DIR__ . '/function.php';
	// connecting to db
	$db = new DB_CONNECT();
	
	session_start();
	if(isset($_GET['exit']))
	{
		session_unset();
		session_destroy();
		session_write_close();
		setcookie(session_name(),'',0,'/');
		session_regenerate_id(true);
		header('Location: '.$_SERVER['HTTP_REFERER']);
	}	
	if(isset($_SESSION['msg']) && !empty($_SESSION['msg']))
	{
		$tempMsg = $_SESSION['msg'];
		$_SESSION['msg'] = false;
	}

?>

<html>
	<head>
		<link rel="stylesheet" type="text/css" media="screen" href="css/style.css">
	    <link href="http://netdna.bootstrapcdn.com/twitter-bootstrap/2.2.2/css/bootstrap-combined.min.css" rel="stylesheet">
		<link rel="stylesheet" type="text/css" media="screen" href="http://tarruda.github.com/bootstrap-datetimepicker/assets/css/bootstrap-datetimepicker.min.css">
		<link href='http://fonts.googleapis.com/css?family=Open+Sans' rel='stylesheet' type='text/css'>
		<link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
	</head>
	<body>
		<header>
			<div id="contentHead">Alarma Compartida!</div>
			<?php 
				if(isset($_SESSION['id_user']) || !empty($_SESSION['id_user']))
				{
					$nombre = $_SESSION['nombre_user'];
					$aut = $_SESSION['autentificador'];
					echo '<div id="blocBienvenida">
						<div id="bineNombre">
							<b>Bienvenido '.$nombre.'</b> // Codigo autentificador: '.$aut.'
						</div>
						<div id="salida">
							<a href="?exit">Salida</a>
						</div>
					</div>';		
				}
			?>
		</header>
			<div id="contGeneral">
				<div id="contentTime">
				<?php
					if(!isset($_SESSION['id_user']) || empty($_SESSION['id_user']))
					{
						if(isset($_GET['regist']))
						{
							echo '<div id="registro">';
							if(isset($_POST['registroForm']))
							{
								if(empty($_POST['name']) || empty($_POST['email']) || empty($_POST['pass']))
								{
									$_SESSION['msg'] = "Todos los campos son requeridos...";
									header('Location: '.$_SERVER['REQUEST_URI']);	
								}
								else
								{
									$email = $_POST['email'];
									$pass = $_POST['pass'];
									$name = $_POST['name'];
									
									$est = createUser($name,$email,$pass);
									if($est === true)
									{										
										echo "Bienvenido ". $name;
										echo '<br/><a href="'. $_SERVER['HTTP_REFERER'] .'">Iniciar sesion</a>';
									}
									else
									{
										//Correo registrado.
										$_SESSION['msg'] = $est;
										header('Location: '.$_SERVER['REQUEST_URI']);	
									}
								}
							}
							else
							{				
								echo formRegist();
							}
							echo '</div>';
						}
						else
						{
							echo '<div id="login">';
							if (isset($_POST['secionIniciada']))
							{
								if(empty($_POST['email']) || empty($_POST['pass']))
								{
									$_SESSION['msg'] = "Nombre de usuario y contraseña son requeridos...";
									header('Location: '.$_SERVER['REQUEST_URI']);	
								}
								else
								{
									$email = $_POST['email'];
									$pass = $_POST['pass'];
									
									$est = validUser($email, $pass);
									
									if(is_array($est))
									{
										$_SESSION['id_user'] = $est['ID'];
										$_SESSION['nombre_user'] = $est['NAME'];
										$_SESSION['autentificador'] = $est['AUTENT'];
										header('Location: '.$_SERVER['REQUEST_URI']);										
									}
									else
									{
										$_SESSION['msg'] = "Correo y contraseña no coinciden...";
										header('Location: '.$_SERVER['REQUEST_URI']);	
									}
								}
							}
							else
							{			
								echo formLogin();
							}
							echo '</div>';
						}
					}
					else
					{
						setTime();
					}
				?>
			
				</div>
			</div>
		<footer>B1D4 Copyright © All Rights Reserved 2015</footer>
		<script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script> 
		<script type="text/javascript" src="http://netdna.bootstrapcdn.com/twitter-bootstrap/2.2.2/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="http://tarruda.github.com/bootstrap-datetimepicker/assets/js/bootstrap-datetimepicker.min.js"></script>
		<script type="text/javascript" src="http://tarruda.github.com/bootstrap-datetimepicker/assets/js/bootstrap-datetimepicker.pt-BR.js"></script>
		<script type="text/javascript">
			$('#datetimepicker').datetimepicker({
				format: 'yyyy-MM-dd hh:mm:ss',
				language: 'es'
			});
		</script>	
		<?php
			if(isset($tempMsg) && !empty($tempMsg))
			{
				echo '
					<script type="text/javascript">
						alert("'.$tempMsg.'");
					</script>
				';
				$_SESSION['msg'] = false;
			}
		?>
	</body>
</html>