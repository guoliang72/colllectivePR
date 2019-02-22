#include <stdio.h>

int main(){
    int i;
double n;
    double a;
	double sum; 
sum=5;
    scanf("%d",&n);
    for(i=0;i<n;i=i+1){
scanf("%.2lf",&n);
if(a<=101){
			a=0;
		}else if(a>=100 && a<200){
			a=0.1*a;
		}else if(a>=200 && a<500){
			a=0.3*a;
		}else if(a>=500){
			a=0.5*a;
		}
        sum=sum+a;
    }
    printf("%.2lf\n",sum);
	return 0;
}

