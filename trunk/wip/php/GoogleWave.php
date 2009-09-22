<?php
# Copyright (C) 2008 Mark Johnston and Adam Mckaig
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
#
# http://www.gnu.org/licenses/gpl-3.0.html

if (!defined('MEDIAWIKI'))
	die();

$wgExtensionCredits['other'][] = array(
	'name'           => 'GoogleWave',
	'author'         => array( 'Tom Maaswinkel, Kim Bruning' ),
	'version'        => '0.2',
	'description'    => 'Integrate Google wave + mediawiki',
);


$wgHooks['APIEditBeforeSave'][]="GoogleWave_APIEditBeforeSave";
$wgHooks['ArticleSaveComplete'][]="GoogleWave_ArticleSaveComplete";
$wgHooks['ArticleInsertComplete'][]="GoogleWave_ArticleInsertComplete";

global $gw_wave_id;
$gw_wave_id=null;

function GoogleWave_ArticleSaveComplete( &$article, &$user, $text, $summary,
 &$minoredit, $watchthis, $sectionanchor, &$flags, $revision, &$status, $baseRevId ) {
	$page_id=$article->getID();
	GoogleWave_waveID($page_id, "ArticleSaveComplete");
	return true;
 }

function GoogleWave_APIEditBeforeSave(&$EditPage, $text, &$resultArr) {
	$articleID=$EditPage->mTitle->getArticleID();
	GoogleWave_waveID($articleID,"APIEditBeforeSave");
	return true;
}

function GoogleWave_ArticleInsertComplete( &$article, &$user, $text, $summary, $minoredit, 
$watchthis, $sectionanchor, &$flags, $revision ) { 
	$page_id=$article->getID();
	GoogleWave_waveID($page_id,"ArticleInsertComplete");
	return true;
}


function GoogleWave_waveId($page_id, $debugstr) {
	global $wgRequest, $gw_wave_id;
	$logfile=fopen("/home/kim/logging/alog","a");
	date_default_timezone_set("UTC");
	fwrite($logfile,date("l jS \of F Y h:i:s A")."\n");
	fwrite($logfile,"ola\n");
	#fwrite($logfile,print_r( $wgRequest, true));
	fwrite($logfile,"debug: ".$debugstr."\n");
	fwrite($logfile,"page: ".$page_id."\n");

	$wave_in_url=$wgRequest->getText("wave_id");
	$wave_in_url=trim($wave_in_url);

	fwrite($logfile,"rq: -".$wave_in_url."-\n");

	$dbr = wfGetDB( DB_SLAVE );
	$wave_in_database = $dbr->selectField("wave","wave_id","page_id=$page_id","GoogleWave_waveId");
	
	
	fwrite($logfile,"db: -".$wave_in_database."- \n");
	fwrite($logfile,"gl: -".$gw_wave_id."- \n");

	$wave_id=$wave_in_url;
	if (!$wave_id)
		$wave_id=$gw_wave_id;
	$gw_wave_id=$wave_id;

	fwrite($logfile,"fl: -".$wave_id."- \n");
	fclose($logfile);

	if (!($wave_in_database) && $wave_id) {
		fwrite($logfile,"inserting\n");
		$dbr->insert("wave",array("page_id"=>$page_id,"wave_id"=>$wave_id));
	}
}


$wgHooks['AlternateEdit'][] = "GoogleWave_turnOffToolbar";
function GoogleWave_turnOffToolbar($editPage) {
	global $wgUser, $wgHooks, $wgRequest;

	/* if the user has the edit toolbar turned on, then turn
	 * it off (to hide the ugly toolbar), and add the BeforePageDisplay
	 * hook, too attach our replacement */
	if($wgUser->getOption('showtoolbar')) {
		$wave_in_url=$wgRequest->getText("wave_id");
		$wave_in_url=trim($wave_in_url);
		/*TODO might be wise to do extra checking on the wave_id here
		we should not simply trust.*/	

		$articleID=$editPage->mTitle->getArticleID();
		$dbr = wfGetDB( DB_SLAVE );
		$wave_in_database = $dbr->selectField("wave","wave_id","page_id=$articleID","GoogleWave_turnOffToolbar");

		$wave=null;
		if ($wave_in_database) {
			$wave=$wave_in_database;
		} elseif ($wave_in_url) {
			$dbr->insert("wave",array("page_id"=>$articleID,"wave_id"=>$wave_in_url));
			$wave=$wave_in_url;
		} else {
			/* NOTE No associated wave found. We'd like to make a wave now, but
			There's no option in the embed API to make a wave yet. If the wave team adds 
			such an option, we can create the wave here. for now, we just do nothing. */
		}
		
		if ($articleID==0)
			return true;

		global $wgOut;
		if ($wave) {
			GoogleWave_ShowUser($wave,$wgOut);
			return false;
		}
		#if($editPage->mTitle == "Elephants")
		#	$wgHooks['BeforePageDisplay'][] = array('GoogleWave_ShowUser','wavesandbox.com!w+GK0H-KpP%A');

		#if($editPage->mTitle == "Wave4")
		#	$wgHooks['BeforePageDisplay'][] = array('GoogleWave_ShowUser','wavesandbox.com!w+GK0H-KpP%A');

		#if($editPage->mTitle == "Wave3")
		#	$wgHooks['BeforePageDisplay'][] = array('GoogleWave_ShowUser','wavesandbox.com!w+Zse99PyY%B');

	}

	return true;
}

function GoogleWave_ShowUser($waveid,&$out) {
	global $wgTitle;
	$pageid=$wgTitle->getArticleID();
	$out->addHTML("<p><b>PageID:</b><span style='color:#00aa00;'>$pageid</span></p>");
	$out->addHTML("<p><b>WaveID:</b><span style='color:#00aa00;'>$waveid</span></p>");
	$out->addHTML("<div id='room_for_wave_panel' style='width: 100%; height: 500px' ></div>");
	$out->addScript('<script src="http://wave-api.appspot.com/public/embed.js" type="text/javascript"></script>');

	$out->addInlineScript("
		function initialize() {
		/* document.getElementById('toolbar').innerHTML = '';
		document.getElementById('editform').innerHTML = ''; */
		var wavePanel = new WavePanel('http://wave.google.com/a/wavesandbox.com/');
		wavePanel.loadWave('".$waveid."');
		/*wavePanel.init(document.getElementById('editform'));*/
		wavePanel.init(document.getElementById('room_for_wave_panel'));
		/*wavePanel.init();*/
		wavePanel.addParticipant();
	}");
	$out->addInlineScript("
		checkLoad();
		function checkLoad()
		{
			 if (document.getElementById('room_for_wave_panel') != null)
			 {
				  // call form population script
				 initialize();
			 } else {
				  setTimeout('checkLoad();', 1000)
			 }
		}");
	return true;

}
