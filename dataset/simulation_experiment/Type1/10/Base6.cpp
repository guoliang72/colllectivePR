#include <stdio.h>

int main(){
    int i;
double n;
    double a;
	double sum; 
	sum=0;
scanf("%lf",&n);
for(i=2;i<n;i=i+1){
        scanf("%lf",&a);
if(a<101){
a=100;
		}else if(a>=100 && a<200){
			a=0.1*a;
		}else if(a>=200 && a<500){
			a=0.3*a;
}else if(a>500){
a=0.3*a;
		}
        sum=sum+a;
    }
    printf("%.2lf\n",sum);
	return 0;
}

