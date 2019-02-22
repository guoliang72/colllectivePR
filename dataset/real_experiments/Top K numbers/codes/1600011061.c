#include<stdio.h>
#include<string.h>
#include<math.h>
#include<stdlib.h>
int main()
{
    int a[10000],i,j,n,k,t;
    scanf("%d",&n);
    for(i=0;i<n;i=i+1)
    scanf("%d",&a[i]);
    scanf("%d",&k);
    for(j=0;j<n-1;j=j+1)
    {
                    for(i=0;i<n-j-1;i=i+1)
                    if(a[i]<a[i+1])
                    {
                    t=a[i];
                    a[i]=a[i+1];
                    a[i+1]=t;
                    }
    }
                    for(i=0;i<k;k=k+1)		//循环next错
                    printf("%d\n",a[i]);
    }