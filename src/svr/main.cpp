#include "Comm.h"
#include "COption.h"
#include "CServer.h"
#include "CAppConfig.h"


using namespace std;

// use command to verify prog version, such as strings test_svr | grep -i "WCDJ"
// the result likes, WCDJ|VersionV1.0R010_2014/02/20 18:44:38 |gcc_4.1.2 20070115 (prerelease) (SUSE Linux)
#define VERSION "WCDJ|VersionV1.0R010_"MY_DATE" |gcc_"__VERSION__

const unsigned char SIG_SHOW =  1;
const unsigned char SIG_QUIT =  2;
unsigned char SIG_STAT       =  0;


void usage(const string& progname)
{
	cout << "Welcome to use WCDJ-svr, the version is:\n" << VERSION << " \n"
	     << "Usage: " << progname << " [OPTION]..." << "\n"
	     << "       " << progname << " -config=configfile [OPTION]..." << "\n"
	     << "       OPTION format: -name=value" << "\n" << endl;
}

void sigusr1_handle(int iSigVal)
{
	SIG_STAT = SIG_SHOW;
	signal(SIGUSR1, sigusr1_handle);
}

void sigusr2_handle(int iSigVal)
{
	SIG_STAT = SIG_QUIT;
	signal(SIGUSR2, sigusr2_handle);
}

void daemon()
{
	pid_t pid;

	if ((pid = fork()) != 0)
	{
		exit(0);
	}

	setsid();
	signal(SIGINT,    SIG_IGN);
	signal(SIGHUP,    SIG_IGN);
	signal(SIGQUIT,   SIG_IGN);
	signal(SIGPIPE,   SIG_IGN);
	signal(SIGTTOU,   SIG_IGN);
	signal(SIGTTIN,   SIG_IGN);
	signal(SIGCHLD,   SIG_IGN);
	signal(SIGTERM,   SIG_IGN);

	// ignore_pipe();
	struct sigaction sig;
	sig.sa_handler =  SIG_IGN;
	sig.sa_flags   =  0;
	sigemptyset(&sig.sa_mask);
	sigaction(SIGPIPE, &sig, NULL);

	if ((pid = fork()) != 0)
	{
		exit(0);
	}

	//chdir("/");

	umask(0);

#ifdef NOPRINT_TERMINAL
	for (int i = 0; i < 64; ++i)
		close(i);
#else
	for (int i = 3; i < 64; ++i)
		close(i);
#endif

}

int create_pid_file(const char *sPIDFile)
{
	FILE *pstFile;

	pstFile = fopen(sPIDFile, "w");
	if (pstFile == NULL)
	{
		fprintf(stderr, "Failed to open pid file:%s!\n", sPIDFile);
		return E_FAIL;
	}
	else
	{
		fprintf(pstFile, "%d", getpid());
		fclose(pstFile);
	}

	return E_OK;
}

void read_conf(COption &opt, CAppConfig &appconf_instance)
{
	// if find config file, then read firstly
	if (!opt["config"].empty()) {
		// TODO
	}

	// after reading config from file then read terminal para
	if (!opt["projecthome"].empty()) {
		appconf_instance.set_projecthome(opt["projecthome"].c_str());
	}

	if (!opt["clientsvmqkey"].empty()) {
		appconf_instance.set_clientsvmqkey(atoi(opt["clientsvmqkey"].c_str()));
	}

	if (!opt["serversvmqkey"].empty()) {
		appconf_instance.set_serversvmqkey(atoi(opt["serversvmqkey"].c_str()));
	}

	return;
}

/*
 * program entry
 * */
int main(int argc, char **argv)
{
	// check input
	if (argc < 2) {
		usage(argv[0]);
		return E_FAIL;
	}

	// parse input paras
	COption opt;
	opt.read_arg(argc, argv);

	// init conf
	CAppConfig& appconf_instance = CAppConfig::getapp_config_instance();
	read_conf(opt, appconf_instance);

	// check paras
	appconf_instance.check_conf();

	// set to daemon process
	daemon();

	// prevent multi instance
	string strHome    =  opt["projecthome"];
	string strProg    =  argv[0];
	string strPidFile =  strHome + "/bin/" + strProg + ".pid";
	if (create_pid_file(strPidFile.c_str()) != 0) {
		cerr << "create_pid_file err, so quit!" << endl;
		return E_FAIL;
	}

	// start server
	try {
		signal(SIGUSR1, sigusr1_handle);
		signal(SIGUSR2, sigusr2_handle);

		CServer* server = new CServer();
		server->init(appconf_instance);
		server->run();

	} catch (runtime_error& e) {
		cerr << "catch runtime_error:" << e.what() << "\n" << endl;
		return E_FAIL;

	} catch (logic_error& e) {
		cerr << "catch logic_error:" << e.what() << "\n" << endl;
		return E_FAIL;
	}

	return E_OK;
}
