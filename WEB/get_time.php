<?php
	 
	/*
	 * Following code will list all the products
	 */
	 
	// array for JSON response
	$response = array();
	 
	// include db connect class
	require_once __DIR__ . '/db_connect.php';

	 
	// connecting to db
	$db = new DB_CONNECT();


	if(!empty($_GET['user']))
	{
		$user = $_GET['user'];
		// get all products from products table
		$query = "SELECT `id`, `user_id`, `tiempo`, `estado` FROM `tiempos` WHERE `user_id` = ". $user ." ORDER BY `id` DESC LIMIT 0 , 10";
		$result = mysql_query($query) or die(mysql_error());
		 
		// check for empty result
		if (mysql_num_rows($result) > 0) 
		{
			// looping through all results
			// products node
			$response["tiempos"] = array();
		 
			while ($row = mysql_fetch_array($result)) 
			{
				// temp user array
				$product = array();
				$product["id"] = $row["id"];
				$product["user_id"] = $row["user_id"];
				$product["tiempo"] = $row["tiempo"];
				$product["estado"] = $row["estado"];
				// push single product into final response array
				array_push($response["tiempos"], $product);
			}
			// success
			$response["success"] = 1;
			 
			// echoing JSON response
			echo json_encode($response);
		}
		else 
		{
			// no products found
			$response["success"] = 0;
			$response["message"] = "No se han encontrado tiempos...";
		 
			// echo no users JSON
			echo json_encode($response);
		}
	}
	if(!empty($_GET['emailAC']) && !empty($_GET['passAC']))
	{
		$userEmail = $_GET['emailAC'];
		$passAC = $_GET['passAC'];
				
		$query = "SELECT `id`,`name`,`autentification` FROM `user` WHERE `email` = '". $userEmail ."' AND `pass` = '". $passAC ."'";
		$result = mysql_query($query) or die(mysql_error());
		
		// check for empty result
		if (mysql_num_rows($result) > 0) 
		{
			// looping through all results
			// products node
			$response["user"] = array();
		 
			while ($row = mysql_fetch_array($result)) 
			{
				// temp user array
				$product = array();
				$product["id"] = $row["id"];
				$product["name"] = $row["name"];
				$product["autentification"] = $row["autentification"];
				// push single product into final response array
				array_push($response["user"], $product);
			}
			// success
			$response["success"] = 1;
			 
			// echoing JSON response
			echo json_encode($response);
		}
		else 
		{
			// no products found
			$response["success"] = 0;
			$response["message"] = "No se han encontrado usuarios...";
		 
			// echo no users JSON
			echo json_encode($response);
		}
	}
?>