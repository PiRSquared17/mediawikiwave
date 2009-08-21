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
	'author'         => array( 'Tom Maaswinkel' ),
	'version'        => '0.1',
	'description'    => 'Prototyping google wave + mediawiki',
);


$wgHooks['EditPage::showEditForm:initial'][] = "GoogleWave_turnOffToolbar";
function GoogleWave_turnOffToolbar($editPage) {
	global $wgUser, $wgHooks;

	/* if the user has the edit toolbar turned on, then turn
	 * it off (to hide the ugly toolbar), and add the BeforePageDisplay
	 * hook, too attach our replacement */
	if($wgUser->getOption('showtoolbar')) {
		
		if($editPage->mTitle == "User:TheDevilOnLine")
			$wgHooks['BeforePageDisplay'][] = array('GoogleWave_ShowUser','wavesandbox.com!w+E7oScQSe%D');
		elseif($editPage->mTitle == "Elephants")
			$wgHooks['BeforePageDisplay'][] = array('GoogleWave_ShowUser','wavesandbox.com!w+GK0H-KpP%A');
		elseif($editPage->mTitle == "Google Wave")
			$wgHooks['BeforePageDisplay'][] = array('GoogleWave_ShowUser','wavesandbox.com!w+GK0H-KpP%C');
		
		
	}

	return true;
}

function GoogleWave_ShowUser($waveid,&$out) {
	$out->addScript('<script src="http://wave-api.appspot.com/public/embed.js" type="text/javascript"></script>');
	$out->addInlineScript("function initialize() {
		document.getElementById('toolbar').innerHTML = '';
		document.getElementById('editform').innerHTML = '';
		var wavePanel = new WavePanel('http://wave.google.com/a/wavesandbox.com/');
		wavePanel.loadWave('".$waveid."');
		wavePanel.init(document.getElementById('editform'));
		wavePanel.addParticipant();
	}");
	$out->addInlineScript("
		checkLoad();
		function checkLoad()
		{
			 if (document.getElementById('editform') != null && document.getElementById('toolbar'))
			 {
				  // call form population script
				 initialize();
			 } else {
				  setTimeout('checkLoad();', 1000)
			 }
		}");
	return true;

}