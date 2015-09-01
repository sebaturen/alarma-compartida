<?php 

	function formRegist()
	{
		return '
			<form method="post" action="">
				<table>
					<tbody>
						<tr>
							<td>Email:</td>
							<td><input class="changeSize" type="text" name="email" /></td>
							<td> *Este se utiliza para inicair secion</td>
						</tr>	
						<tr>
							<td>Password:</td>
							<td><input class="changeSize" type="password" name="pass"/></td>
						</tr>
						<tr>
							<td>Nombre Completo:</td>
							<td><input class="changeSize" type="text" name="name"/></td>
						</tr>					
					</tbody>
				</table>
				<input type="hidden" name="registroForm" value="true"/> 
				<p><input type="submit" value="Registrarse"/></p>
			</form>';
	}
	
	function formLogin()
	{
		return '
			<form method="post" action="">
				<table>
					<tbody>
						<tr>
							<td>Email:</td>
							<td><input class="changeSize" type="text" name="email" /></td>
						</tr>	
						<tr>
							<td>Password:</td>
							<td><input class="changeSize" type="password" name="pass"/></td>
						</tr>					
					</tbody>
				</table>
				<input type="hidden" name="secionIniciada" value="true"/> 
				<input type="submit" value="Entrar"/>
			</form>
			<br/>
			<a href="?regist">Registrate</a>';
	}
	
	function setTime()
	{
		if(!isset($_SESSION['id_user']) || empty($_SESSION['id_user']))
		{
			echo "Existe un problema en tu secion, por favor vuelve a iniciar<br/>";
			echo formLogin();
		}
		else
		{
			$nombre = $_SESSION['nombre_user'];
			$idUser = $_SESSION['id_user'];
						
			//Asignar tiempo	
			echo '<div id="contentUser">';
				echo '<div id="asignAlarmas">';
					echo '<div class="columnaUser">';
						echo "Asigna un tiempo!<br/>";			
						if(isset($_POST['asignarTiempo']))
						{
							if(!empty($_POST['fecha']) && !empty($_POST['userName']) && $_POST['userName'] != 0)
							{
								$userHacia = $_POST['userName'];
								$fecha = $_POST['fecha'];
								
								//Validando si es amigo
								if(isAmigos($idUser, $userHacia))
								{
									$query = "INSERT INTO `tiempos` (`user_id`,`tiempo`,`user_id_asignador`) VALUES (".$userHacia.",'".$fecha."',".$idUser.");";
									$result = mysql_query($query) or die(mysql_error());									
									$_SESSION['msg'] = "Alarma Agregada!";
									header('Location: '.$_SERVER['HTTP_REFERER']);											
								}
								else
								{
									$_SESSION['msg'] = "El(ella) no es tu amigo(a)...";
									header('Location: '.$_SERVER['HTTP_REFERER']);										
								}
							}
							else
							{
								$_SESSION['msg'] = "Debe selecionar una fecha y un usuario!";
								header('Location: '.$_SERVER['HTTP_REFERER']);	
							}
						}
						echo formularioAsignacion();
						if(isset($_POST['addFriend']))
						{
							if(!empty($_POST['email']) && !empty($_POST['autentifiador']))
							{
								$email = $_POST['email'];
								$autentificador = $_POST['autentifiador'];
								
								$query = "SELECT `id` FROM `user` WHERE email = '".$email."' AND autentification = '".$autentificador."'";
								$result = mysql_query($query) or die(mysql_error());
								if(mysql_num_rows($result) != 0)
								{
									$userFriend = mysql_fetch_object($result);
									$query = "INSERT INTO `friend_list` (`user_id`,`user_friend_id`) VALUES (".$idUser.",".$userFriend->id.");";
									$result = mysql_query($query) or die(mysql_error());
									
									$_SESSION['msg'] = "Amigo agregado!";
									header('Location: '.$_SERVER['HTTP_REFERER']);										
								}
								else
								{
									$_SESSION['msg'] = "Email y autentificador no concuerdan";
									header('Location: '.$_SERVER['HTTP_REFERER']);	
								}								
							}
							else
							{
								$_SESSION['msg'] = "Email y autentificador son requeridos!";
								header('Location: '.$_SERVER['HTTP_REFERER']);
							}							
						}
						echo "Registra a un amigo!<br/>";
						echo formRegistFriend();
					echo '</div>';
				echo '</div>';
			
				//Desactivar alarma...
				echo '<div id="listaAlarmas">';
					echo '<div class="columnaUser">';
						if(isset($_POST['destAlarma']))
						{
							$complet = true;
							foreach($_POST as $key => $val)
							{
								if(is_numeric(substr($key, 0)))
								{
									$complet = disableAlarm($key,$idUser);						
								}
							}
							if(!$complet)
							{
								$_SESSION['msg'] = "Algunas alarmas no se an podido desactivar";
							}
							else
							{
								$_SESSION['msg'] = "Alarma Desactivada!";								
							}
							header('Location: '.$_SERVER['HTTP_REFERER']);
						}
						echo formularioAlarmas($idUser);
					echo '</div>';
				echo '</div>';
			echo '</div>';
			
		}
	}
	
	function disableAlarm($id, $idUser)
	{
		$alarma = getInfoAlarm($id);
		if(!$alarm)
		{
			if($alarma->getUserAsignadoID() == $idUser || $alarma->getUserID() == $idUser)
			{
				$query = "UPDATE `tiempos` SET `estado` = FALSE WHERE `id` = ". $id;
				$result = mysql_query($query) or die(mysql_error());	
				return true;
			}			
		}
		return false;
	}
	
	function formularioAlarmas($idUser)
	{
		$val = '';
		$val .= '<form method="post" action="">';	
				
		//Lista de alarmas que programe YO
		$val .= '<div class="tablaAlarma">';
			$val .= '<div class="titleAlarmList"><b>Alarmas que sonaran en tu telefono</b></div>';
			$val .= tablaAlarmas(listAlarmPropia($idUser));
		$val .= '</div>';
		//Lista de alarmas que tengo para MI
		$val .= '<div class="tablaAlarma">';
			$val .= '<div class="titleAlarmList"><b>Alarmas que tu asignaste</b></div>';
			$val .= tablaAlarmas(listAlarmAsigned($idUser));
		$val .= '</div>';
		
		//cierre formulario
		$val .= '
			<input type="hidden" name="destAlarma" value="true"/> 
			<input type="submit" value="Desactivar Alarma"/><br/>
			*desactiva todas las alarmas selecionadas	
		';
		
		$val .= '</form>';
		
		return $val;
	}
	
	function formularioAsignacion()
	{
		$val = '';
		$val .= '
			<form method="post" action="">
				'.inputDateTime().'
				'.listUserSelectFormat($_SESSION['id_user']).'
				<input class="changeSize" type="hidden" name="asignarTiempo" value="true"/> 
				<p><input type="submit" value="Registrar"/></p>				
			</form>
		';
		
		return $val;
	}
	
	function formRegistFriend()
	{
		$val = '';
		$val .= '
			<form method="post" action="">
				Email:<br/>
				<input class="changeSize" type="text" name="email" /><br/>
				Autentificador:<br/>
				<input class="changeSize" type="text" name="autentifiador" />
				<input class="changeSize" type="hidden" name="addFriend" value="true"/> 
				<p><input type="submit" value="Registrar"/></p>				
			</form>';
		return $val;
	}
	
	function inputDateTime()
	{
		return '
			<div id="datetimepicker" class="input-append date">
			  <input type="text" name="fecha" class="changeSize"></input>
			  <span class="add-on">
				<i data-time-icon="icon-time" data-date-icon="icon-calendar"></i>
			  </span>
			</div>
		';
	}
	
	function listUserSelectFormat($idUser)
	{
		$val = '';
		$val .= '
			<select id="userList" name="userName">
				<option value="0">Selecione un amigo!</option>
		';
		foreach(listUsers($idUser) as $usr)
		{
			$val .= '
				<option value="'.$usr['ID'].'">'.$usr['NOMBRE'].'</option>
			';
		}
		$val .= '
			</select>
		';
		
		return $val;
	}
	
	function listUsers($idUser)
	{		
		$query = "
				SELECT u2.name, u2.id
				FROM user u1, friend_list fl, user u2
				WHERE
						u1.id = ".$idUser."
					AND u1.id = fl.user_id
					AND fl.user_friend_id = u2.id
		";
		$result = mysql_query($query) or die(mysql_error());
		
		//Revisando si el usuario exiset...
		if(mysql_num_rows($result) != 0)
		{
			$users = array();
			while($usr = mysql_fetch_object($result))
			{
				array_push($users, array('ID' => $usr->id,'NOMBRE' => $usr->name));
			}
			return $users;
		}
		else
		{
			return "No se han encontrado alarmas...<br/>";
		}
	}
	
	function tablaAlarmas($arrayLista)
	{
		$cont = '';
		$cont .= '
				<table class="tablaAlarmas">
					<thead>
						<tr>
							<th id="select"></th>
							<th id="tiempo">Tiempo</th>
							<th id="usuario_target">Usuario TARGET</th>
							<th id="usuario_caster">Usuario CASTE</th>
							<th id="estado">Estado</th>
						</tr>
					</thead>
					<tbody>			
			';
			foreach($arrayLista as $alarma)
			{
				$arCont = $alarma->getAlarma();
				$cont .= '
						<tr>
							<td><input id="chekAlarm" type="checkbox" name="'.$arCont['ID'].'"></td>
							<td>'.$arCont['TIEMPO'].'</td>
							<td>'.getUserName($arCont['USER_ID']).'</td>
							<td>'.getUserName($arCont['USER_ASIGNADOR']).'</td>
							<td>'.($arCont['ESTADO']? 'Activada':'Desactivada').'</td>
						</tr>
				';
			}		
			$cont .= '
					</tbody>
				</table>
			';
		return $cont;
	}
	
	function listAlarmAsigned($idUser)
	{
		$query = "SELECT `id`,`user_id`,`tiempo`,`estado`,`user_id_asignador` FROM `tiempos` WHERE `user_id_asignador` = ". $idUser ." ORDER BY `id` DESC LIMIT 0 , 10";
		return listAlarm($query);
	}
	
	function listAlarmPropia($idUser)
	{
		$query = "SELECT `id`,`user_id`,`tiempo`,`estado`,`user_id_asignador`  FROM `tiempos` WHERE `user_id` = ". $idUser ." ORDER BY `id` DESC LIMIT 0 , 10";
		return listAlarm($query);		
	}
	
	function listAlarm($query)
	{
		$result = mysql_query($query) or die(mysql_error());
				
		//Revisando si el usuario exiset...
		if(mysql_num_rows($result) != 0)
		{
			$alarmasUser = array();
			while($alr = mysql_fetch_object($result))
			{
				array_push($alarmasUser, new Alarma($alr->id, $alr->tiempo, $alr->user_id, $alr->user_id_asignador, $alr->estado));
			}
			return $alarmasUser;
		}
		else
		{
			return "No se han encontrado alarmas...<br/>";
		}		
	}
	
	function getUserName($idUser)
	{
		$query = "SELECT `name` FROM `user` WHERE `id` = ". $idUser ." ORDER BY `id` DESC LIMIT 0 , 10";
		$result = mysql_query($query) or die(mysql_error());
				
		//Revisando si el usuario exiset...
		if(mysql_num_rows($result) != 0)
		{
			$usr = mysql_fetch_object($result);
			return $usr->name;
		}
		else
		{
			return "No se han encontrado usuarios...<br/>";
		}
	}
	
	class Alarma
	{
		private $id;
		private $tiempo;
		private $user_id;
		private $user_asignador;
		private $estado;
		
		//Constructor
		function __construct($id, $tiempo, $user_id, $user_asignador, $estado) 
		{
			$this->id = $id;
			$this->tiempo = $tiempo;
			$this->user_id = $user_id;
			$this->user_asignador = $user_asignador;
			$this->estado = ($estado == 1) ? true:false;
		}
		
		public function getAlarma()
		{
			return array(
					'ID'				=> $this->id,
					'TIEMPO' 			=> $this->tiempo,
					'USER_ID' 			=> $this->user_id,
					'ESTADO'			=> $this->estado,
					'USER_ASIGNADOR'	=> $this->user_asignador
				);
		}
		
		public function getUserID()
		{
			return $this->user_id;
		}
		
		public function getUserAsignadoID()
		{
			return $this->user_asignador;
		}
		
	}	
	
	//Creditos de la funcion a http://stackoverflow.com/questions/6101956/generating-a-random-password-in-php
	function randomPassword() 
	{
		$alphabet = "abcdefghijklmnopqrstuwxyzABCDEFGHIJKLMNOPQRSTUWXYZ0123456789";
		$pass = array(); //remember to declare $pass as an array
		$alphaLength = strlen($alphabet) - 1; //put the length -1 in cache
		for ($i = 0; $i < 8; $i++) 
		{
			$n = rand(0, $alphaLength);
			$pass[] = $alphabet[$n];
		}
		return implode($pass); //turn the array into a string
	}

	function createUser($name, $email, $pass)
	{
		if(filter_var($email, FILTER_VALIDATE_EMAIL))
		{
			$query = "SELECT `id` FROM `user` WHERE `email` = '". $email."' ORDER BY `id` DESC LIMIT 0 , 10";
			$result = mysql_query($query) or die(mysql_error());
			
			//Revisando si el usuario exiset...
			if(mysql_num_rows($result) == 0)
			{
				//Usuario existe
				$query = "INSERT INTO `user` (`name`,`email`,`pass`,`autentification`) VALUES ('".$name."','".$email."','".$pass."','".randomPassword()."');";
				$result = mysql_query($query) or die(mysql_error());
				return true;
			}
			else
			{
				//Correo registrado.
				return "El correo indicado esta en uso, prueba con otro";
			}			
		}
		else
		{
			return "Correo no valido!";
		}
	}
	
	function isAmigos($idUser, $idAmigo)
	{
		//Validando si es amigo
		$query = "SELECT `id` FROM friend_list WHERE `user_id` = ".$idUser." AND user_friend_id = ".$idAmigo;
		$result = mysql_query($query) or die(mysql_error());
		if(mysql_num_rows($result) != 0)
		{
			return true;
		}
		return false;		
	}
	
	function getInfoAlarm($id)
	{
		$query = "SELECT `id`,`user_id`,`tiempo`,`estado`,`user_id_asignador`  FROM `tiempos` WHERE `id` = ". $id;
		$result = mysql_query($query) or die(mysql_error());
		if(mysql_num_rows($result) != 0)
		{
			$alr = mysql_fetch_object($result);
			return (new Alarma($alr->id, $alr->tiempo, $alr->user_id, $alr->user_id_asignador, $alr->estado));
		}		
		return false;
	}
	
	function validUser($email, $pass)
	{
		$query = "SELECT `id`,`name`,`autentification` FROM `user` WHERE `email` = '". $email."' ORDER BY `id` DESC LIMIT 0 , 10";
		$result = mysql_query($query) or die(mysql_error());
		
		//Revisando si el usuario exiset...
		if(mysql_num_rows($result) != 0)
		{
			$user = mysql_fetch_object($result);
			return array(
					'ID' 		=> $user->id,
					'NAME'		=> $user->name,
					'AUTENT'	=> $user->autentification
				);
		}
		else
		{
			return false;
		}
	}
?>