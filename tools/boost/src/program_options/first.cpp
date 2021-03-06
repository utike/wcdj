// Copyright Vladimir Prus 2002-2004.
// Distributed under the Boost Software License, Version 1.0.
// (See accompanying file LICENSE_1_0.txt
// or copy at http://www.boost.org/LICENSE_1_0.txt)

/* The simplest usage of the library.
 */

#include <boost/program_options.hpp>
namespace po = boost::program_options;

#include <iostream>
#include <iterator>
using namespace std;

int main(int ac, char* av[])
{
    try {

        po::options_description desc("Allowed options");
        desc.add_options()
            ("help", "produce help message")
            ("compression", po::value<double>(), "set compression level")
        ;

        po::variables_map vm;        
        po::store(po::parse_command_line(ac, av, desc), vm);
        po::notify(vm);    

        if (vm.count("help")) {
            cout << desc << "\n";
            return 0;
        }

        if (vm.count("compression")) {
            cout << "Compression level was set to " 
                 << vm["compression"].as<double>() << ".\n";
        } else {
            cout << "Compression level was not set.\n";
        }
    }
    catch(exception& e) {
        cerr << "error: " << e.what() << "\n";
        return 1;
    }
    catch(...) {
        cerr << "Exception of unknown type!\n";
    }

    return 0;
}
/*
g++ -o first first.cpp /Users/gerryyang/LAMP/boost_1_57_0/install/boost_1_57_0/lib/libboost_program_options.a -I/Users/gerryyang/LAMP/boost_1_57_0/install/boost_1_57_0/include 

output:
gerryyang@mba:program_options$./first
Compression level was not set.
gerryyang@mba:program_options$./first --help
Allowed options:
  --help                produce help message
  --compression arg     set compression level

gerryyang@mba:program_options$./first --compression 123
Compression level was set to 123.

 */
