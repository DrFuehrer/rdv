RDV can be installed on Ubuntu or Debian systems using the RDV APT repository. RDV requires [Ubuntu 8.04](http://www.ubuntu.com/) or [Debian 5.0](http://www.debian.org/).

## Installing via GUI (Ubuntu) ##

  1. Download [http://paltasoftware.com/paltasoftware-key.asc](http://paltasoftware.com/paltasoftware-key.asc) to your computer.
  1. Open _System -> Administration -> Software Sources_.
  1. Go to the _Third-Party Software_ tab and click **Add...**. You will be asked for an APT line, so paste in the one below and click **Add Source**.
> `deb http://paltasoftware.com/debian stable main`
  1. Go to the _Authentication_ tab and click **Import Key File...**. Select the _paltasoftware-key.asc_ file you downloaded in step 1 and click **OK**.
  1. Click **Close** and then click **Reload** if asked to reload the information about available software.
  1. Open _System -> Adminstration -> Synaptic Package Manager_.
  1. Click **Search** and search for _rdv_.
  1. Find the _rdv_ package and check the box next to it, click **Mark for Installation**, and click **Mark** if asked to select additional packages.
  1. Click **Apply**, then click **Apply** again when asked to confirm the list of packages to install.
  1. The files will be downloaded and installed. Click **Close** when finished and exit the _Synaptic Package Manager_.

## Installing from the command line ##

  1. Add this line to /etc/apt/sources.list.
> `deb http://paltasoftware.com/debian stable main`
  1. Install RDV's public key so APT can verify repository. For those of you who want to check, the id of the key is _DA19FC17_ and the fingerprint is _BAD7 D7B2 3522 7570 936C  50B3 F752 0035 DA19 FC17_.
> `> wget -O - http://paltasoftware.com/paltasoftware-key.asc | sudo apt-key add -`
  1. Update your list of apt sources.
> `> sudo apt-get update`
  1. Download and install RDV.
> `> sudo apt-get install rdv`

## Running RDV ##
RDV can be run from _Application -> Education -> RDV_ menu or via the command line by running:
> `> rdv`