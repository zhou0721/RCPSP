this project is aim to solving the problem RCPSP, we know that is a NP complex problem about schedeling. 
The RCPSP:
	the job(project) can not execute before the end of its preceding jobs. and at one time, the resource can not be executed over its volume.

My solution is to use branch and bound to arbitrate all the possible solutions. And calculate its lower bound and compare with the upper bound. 

At last, it can solve 120 project within the acceptable time
