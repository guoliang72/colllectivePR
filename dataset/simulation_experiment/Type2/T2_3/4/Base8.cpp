#include <stdio.h>

int main(){
    int i;
    int n;
    double a;
	double sum; 
	sum=0;
    scanf("%d",&n);
    for(i=0;i<n;i=i+1){
a=0.1*a;
        if(a<100){
			a=0;
		}else if(a>=100 && a<200){
scanf("%lf",&a);
		}else if(a>=200 && a<500){
			a=0.3*a;
		}else if(a>=500){
sum=sum+a;
		}
a=0.5*a;
    }
    printf("%.2lf\n",sum);
	return 0;
}

